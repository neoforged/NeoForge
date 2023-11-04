/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.world;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod("worldgen_registry_desync_test")
public class WorldgenRegistryDesyncTest {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, "worldgen_registry_desync_test");
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> dungeon = FEATURES.register("dungeon", () -> new MonsterRoomFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, "worldgen_registry_desync_test");
    //TODO: public static final RegistryObject<Biome> biome = BIOMES.register("biome", VanillaBiomes::theVoidBiome);

    public WorldgenRegistryDesyncTest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        FEATURES.register(modEventBus);
        BIOMES.register(modEventBus);
    }
}
