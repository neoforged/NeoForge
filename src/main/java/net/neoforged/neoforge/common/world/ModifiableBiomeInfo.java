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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
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
    public boolean applyBiomeModifiers(final Holder<Biome> biome, final List<BiomeModifier> biomeModifiers, RegistryAccess registryAccess) {
        if (this.modifiedBiomeInfo != null)
            return true;

        BiomeInfo original = this.getOriginalBiomeInfo();
        final BiomeInfo.Builder builder = BiomeInfo.Builder.copyOf(original);
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
     * @param mobSpawnSettings   Mob spawn settings.
     */
    public record BiomeInfo(ClimateSettings climateSettings, BiomeSpecialEffects effects, BiomeGenerationSettings generationSettings, MobSpawnSettings mobSpawnSettings) {
        public static class Builder {
            private ClimateSettingsBuilder climateSettings;
            private BiomeSpecialEffectsBuilder effects;
            private BiomeGenerationSettingsBuilder generationSettings;
            private MobSpawnSettingsBuilder mobSpawnSettings;

            /**
             * @param original the biome to copy
             * @return A ModifiedBiomeInfo.Builder with a copy of the biome's data
             */
            public static Builder copyOf(final BiomeInfo original) {
                final ClimateSettingsBuilder climateBuilder = ClimateSettingsBuilder.copyOf(original.climateSettings());
                final BiomeSpecialEffectsBuilder effectsBuilder = BiomeSpecialEffectsBuilder.copyOf(original.effects());
                final BiomeGenerationSettingsBuilder generationBuilder = new BiomeGenerationSettingsBuilder(original.generationSettings());
                final MobSpawnSettingsBuilder mobSpawnBuilder = new MobSpawnSettingsBuilder(original.mobSpawnSettings());

                return new Builder(
                        climateBuilder,
                        effectsBuilder,
                        generationBuilder,
                        mobSpawnBuilder);
            }

            private Builder(final ClimateSettingsBuilder climateSettings, final BiomeSpecialEffectsBuilder effects, final BiomeGenerationSettingsBuilder generationSettings, final MobSpawnSettingsBuilder mobSpawnSettings) {
                this.climateSettings = climateSettings;
                this.effects = effects;
                this.generationSettings = generationSettings;
                this.mobSpawnSettings = mobSpawnSettings;
            }

            public BiomeInfo build() {
                return new BiomeInfo(this.climateSettings.build(), this.effects.build(), this.generationSettings.build(), this.mobSpawnSettings.build());
            }

            public ClimateSettingsBuilder getClimateSettings() {
                return climateSettings;
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
