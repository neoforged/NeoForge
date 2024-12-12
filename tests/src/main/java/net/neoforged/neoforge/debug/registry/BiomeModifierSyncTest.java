/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.TestListener;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.annotation.WithListener;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

@ForEachTest(groups = BiomeModifierSyncTest.GROUP)
public class BiomeModifierSyncTest {
    public static final String GROUP = "registry";
    public static final RegistrationHelper HELPER = RegistrationHelper.create("neotests_biome_modifier_sync_test");

    private static final int MODIFIED_WATER_COLOR = 0x0000FF;

    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = HELPER.registrar(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS);
    private static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<TestModifier>> MODIFY_BIOME_EFFECTS = BIOME_MODIFIER_SERIALIZERS.register("modify_biome_effects", () -> RecordCodecBuilder.mapCodec(builder -> builder.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(TestModifier::biomes),
            Codec.INT.fieldOf("water_color").forGetter(TestModifier::waterColor)).apply(builder, TestModifier::new)));

    public record TestModifier(HolderSet<Biome> biomes, int waterColor) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.MODIFY && this.biomes.contains(biome)) {
                builder.getSpecialEffects().waterColor(this.waterColor);
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return MODIFY_BIOME_EFFECTS.get();
        }
    }

    @OnInit
    static void init(final TestFramework framework) {
        HELPER.register(framework.modEventBus(), framework.container());
    }

    @TestHolder(description = "Tests if biome modifications are properly synced", side = Dist.CLIENT)
    @WithListener(Listener.class)
    static void biomeModifierSync(final DynamicTest test) {
        ResourceKey<BiomeModifier> modifyTaigaModifier = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(HELPER.modId(), "modify_taiga"));
        HELPER.addClientProvider(event -> new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                new RegistrySetBuilder().add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                    var taigaTag = context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_TAIGA);
                    context.register(modifyTaigaModifier, new TestModifier(
                            taigaTag,
                            MODIFIED_WATER_COLOR));
                }),
                Set.of(HELPER.modId())));
    }

    public static class Listener implements TestListener {
        @Override
        public void onEnabled(TestFramework framework, Test test, @Nullable Entity changer) {
            if (changer == null) {
                return;
            }
            RegistryAccess access = changer.registryAccess();
            access.lookup(Registries.BIOME).ifPresentOrElse(biomes -> {
                var taiga = biomes.getValue(Biomes.TAIGA);
                if (taiga == null) {
                    framework.changeStatus(test, Test.Status.failed("Taiga biome not found"), changer);
                    return;
                }
                if (taiga.getSpecialEffects().getWaterColor() == MODIFIED_WATER_COLOR) {
                    framework.changeStatus(test, Test.Status.passed(), changer);
                } else {
                    framework.changeStatus(test, Test.Status.failed("Taiga biome water color not modified"), changer);
                }
            }, () -> {
                framework.changeStatus(test, Test.Status.failed("Failed to get biome registry"), changer);
            });
        }
    }
}
