/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.MobSpawnSettingsModifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Holds lazy-evaluable modified biome info.
 * Memoizers are not used because it's important to return null
 * without evaluating the biome info if it's accessed outside of a server context.
 */
public class ModifiableBiomeInfo {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final BiomeInfo originalBiomeInfo;
    @Nullable
    private BiomeInfo modifiedBiomeInfo = null;

    /**
     * @param originalBiomeInfo BiomeInfo representing the original state of a biome when the biome was constructed.
     */
    public ModifiableBiomeInfo(final BiomeInfo originalBiomeInfo) {
        this.originalBiomeInfo = originalBiomeInfo;
    }

    /**
     * {@return The modified biome info if modified biome info has been generated, otherwise gets original biome info}
     */
    public BiomeInfo get() {
        return this.modifiedBiomeInfo == null
                ? originalBiomeInfo
                : modifiedBiomeInfo;
    }

    /**
     * {@return The original biome info that the associated biome was created with}
     */
    public BiomeInfo getOriginalBiomeInfo() {
        return this.originalBiomeInfo;
    }

    /**
     * {@return Modified biome info; null if it hasn't been set yet}
     */
    @Nullable
    public BiomeInfo getModifiedBiomeInfo() {
        return this.modifiedBiomeInfo;
    }

    /**
     * Internal NeoForge method. Will do nothing if this modifier had already been applied.
     * Creates and caches the modified biome info.
     * 
     * @param biome          named biome with original data.
     * @param biomeModifiers biome modifiers to apply.
     *
     * @return whether the biome's network-synced data was modified
     */
    @ApiStatus.Internal
    public boolean applyBiomeModifiers(final Holder.Reference<Biome> biome, final List<BiomeModifier> biomeModifiers, RegistryAccess registryAccess) {
        if (this.modifiedBiomeInfo != null)
            return true;

        BiomeInfo original = this.getOriginalBiomeInfo();
        final BiomeInfo.Builder builder = BiomeInfo.Builder.copyOf(biome.getKey(), original);
        for (BiomeModifier.Phase phase : BiomeModifier.Phase.values()) {
            for (BiomeModifier modifier : biomeModifiers) {
                modifier.modify(biome, phase, builder);
            }
        }
        DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        JsonElement originalJson = Biome.NETWORK_CODEC.encodeStart(ops, biome.value()).result().orElse(null);
        this.modifiedBiomeInfo = builder.build();
        JsonElement modifiedJson = Biome.NETWORK_CODEC.encodeStart(ops, biome.value()).result().orElse(null);
        if (originalJson == null || modifiedJson == null) {
            LOGGER.warn("Failed to determine whether biome {} was modified", biome);
            return true;
        }
        return !originalJson.equals(modifiedJson);
    }

    /**
     * Record containing raw biome data.
     * 
     * @param climateSettings    Weather and temperature settings.
     * @param effects            Client-relevant effects for rendering and sound.
     * @param generationSettings Worldgen features and carvers.
     */
    public record BiomeInfo(ClimateSettings climateSettings, EnvironmentAttributeMap attributes, BiomeSpecialEffects effects, BiomeGenerationSettings generationSettings) {
        public static class Builder {
            private final ClimateSettingsBuilder climateSettings;
            private final EnvironmentAttributeMapBuilder attributes;
            private final boolean canModifySpawnSettings;
            private final MobSpawnSettingsBuilder mobSpawnSettings;
            private final BiomeSpecialEffectsBuilder effects;
            private final BiomeGenerationSettingsBuilder generationSettings;

            /**
             * @param original the biome to copy
             * @return A ModifiedBiomeInfo.Builder with a copy of the biome's data
             */
            public static Builder copyOf(ResourceKey<Biome> biomeKey, final BiomeInfo original) {
                ClimateSettingsBuilder climateBuilder = ClimateSettingsBuilder.copyOf(original.climateSettings());
                EnvironmentAttributeMapBuilder attributesBuilder = EnvironmentAttributeMapBuilder.copyOf(original.attributes());
                BiomeSpecialEffectsBuilder effectsBuilder = BiomeSpecialEffectsBuilder.copyOf(original.effects());
                BiomeGenerationSettingsBuilder generationBuilder = new BiomeGenerationSettingsBuilder(original.generationSettings());

                return new Builder(biomeKey, climateBuilder, attributesBuilder, effectsBuilder, generationBuilder);
            }

            private Builder(ResourceKey<Biome> biomeKey, ClimateSettingsBuilder climateSettings, EnvironmentAttributeMapBuilder attributes, BiomeSpecialEffectsBuilder effects, BiomeGenerationSettingsBuilder generationSettings) {
                this.climateSettings = climateSettings;
                this.attributes = attributes;
                EnvironmentAttributeMap.Entry<MobSpawnSettings, ?> entry = attributes.get(EnvironmentAttributes.NATURAL_MOB_SPAWNS);
                this.canModifySpawnSettings = entry == null || entry.modifier() == MobSpawnSettingsModifier.overlay();
                if (!this.canModifySpawnSettings) {
                    // Sanity check in case someone cheeses in a non-standard spawn settings modifier
                    LOGGER.warn("Cannot modify spawn settings of biome {}, found unexpected attribute modifier {}", biomeKey, entry.modifier());
                }
                if (entry != null && this.canModifySpawnSettings) {
                    this.mobSpawnSettings = new MobSpawnSettingsBuilder((MobSpawnSettings) entry.argument());
                } else {
                    this.mobSpawnSettings = new MobSpawnSettingsBuilder(MobSpawnSettings.EMPTY);
                }
                this.effects = effects;
                this.generationSettings = generationSettings;
            }

            public BiomeInfo build() {
                if (this.canModifySpawnSettings) {
                    this.attributes.modify(EnvironmentAttributes.NATURAL_MOB_SPAWNS, MobSpawnSettingsModifier.overlay(), this.mobSpawnSettings.build());
                }
                return new BiomeInfo(this.climateSettings.build(), this.attributes.build(), this.effects.build(), this.generationSettings.build());
            }

            public ClimateSettingsBuilder getClimateSettings() {
                return climateSettings;
            }

            public EnvironmentAttributeMapBuilder getAttributes() {
                return attributes;
            }

            public BiomeSpecialEffectsBuilder getSpecialEffects() {
                return effects;
            }

            public BiomeGenerationSettingsBuilder getGenerationSettings() {
                return generationSettings;
            }

            public MobSpawnSettingsBuilder getMobSpawnSettings() {
                return mobSpawnSettings;
            }
        }
    }
}
