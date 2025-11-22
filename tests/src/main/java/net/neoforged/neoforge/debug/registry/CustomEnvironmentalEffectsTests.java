/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.registry;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "registry")
public class CustomEnvironmentalEffectsTests {
    public static final RegistrationHelper HELPER = RegistrationHelper.create("neotests_env_effects");
    private static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(HELPER.modId(), "test_biome"));
    static final Identifier CUSTOM_CLOUDS_ID = Identifier.parse("x:custom_clouds");
    static final Identifier CUSTOM_SKYBOX_ID = Identifier.parse("x:custom_skybox");
    static final Identifier CUSTOM_WEATHER_EFFECTS_ID = Identifier.parse("x:custom_weather_effects");

    @OnInit
    static void init(final TestFramework framework) {
        HELPER.register(framework.modEventBus(), framework.container());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests a biome using the NeoForge environment attributes")
    static void customBiomeAttributeTest(final DynamicTest test) {
        HELPER.addClientProvider(event -> new DatapackBuiltinEntriesProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), createDatapackEntriesBuilder(), Set.of(HELPER.modId())));

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    helper.setBiome(BIOME_KEY);
                    var pos = helper.absolutePos(BlockPos.ZERO);

                    var attributes = helper.getLevel().environmentAttributes();
                    var customSkybox = attributes.getValue(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, pos);
                    helper.assertValueEqual(CUSTOM_SKYBOX_ID, customSkybox, "custom skybox");
                    var customWeatherEffects = attributes.getValue(NeoForgeEnvironmentAttributes.CUSTOM_WEATHER_EFFECTS, pos);
                    helper.assertValueEqual(CUSTOM_WEATHER_EFFECTS_ID, customWeatherEffects, "custom weather effects");
                    var customClouds = attributes.getValue(NeoForgeEnvironmentAttributes.CUSTOM_CLOUDS, pos);
                    helper.assertValueEqual(CUSTOM_CLOUDS_ID, customClouds, "custom clouds");

                    // check that for an arbitrary location it returns the defaults
                    helper.assertValueEqual(NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_SKYBOX, attributes.getValue(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, BlockPos.ZERO), "default skybox");
                    helper.assertValueEqual(NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_WEATHER_EFFECTS, attributes.getValue(NeoForgeEnvironmentAttributes.CUSTOM_WEATHER_EFFECTS, BlockPos.ZERO), "default weather effects");
                    helper.assertValueEqual(NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_CLOUDS, attributes.getValue(NeoForgeEnvironmentAttributes.CUSTOM_CLOUDS, BlockPos.ZERO), "default clouds");
                })
                .thenSucceed());
    }

    /**
     * Builds a biome that actually references the custom effects
     */
    private static RegistrySetBuilder createDatapackEntriesBuilder() {
        return new RegistrySetBuilder()
                .add(Registries.BIOME, context -> {
                    var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
                    var configuredCarvers = context.lookup(Registries.CONFIGURED_CARVER);

                    Biome biome = new Biome.BiomeBuilder()
                            .setAttribute(NeoForgeEnvironmentAttributes.CUSTOM_CLOUDS, CUSTOM_CLOUDS_ID)
                            .setAttribute(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, CUSTOM_SKYBOX_ID)
                            .setAttribute(NeoForgeEnvironmentAttributes.CUSTOM_WEATHER_EFFECTS, CUSTOM_WEATHER_EFFECTS_ID)
                            .generationSettings(new BiomeGenerationSettings.Builder(placedFeatures, configuredCarvers).build())
                            .hasPrecipitation(false)
                            // Copied from the vanilla void biome
                            .temperature(0.5F).downfall(0.5F)
                            .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                            .mobSpawnSettings(new MobSpawnSettings.Builder().creatureGenerationProbability(0).build()).build();

                    context.register(BIOME_KEY, biome);
                });
    }
}
