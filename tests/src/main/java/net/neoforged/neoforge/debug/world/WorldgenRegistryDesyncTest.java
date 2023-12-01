/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.world;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("worldgen_registry_desync_test")
public class WorldgenRegistryDesyncTest {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "worldgen_registry_desync_test");
    public static final Holder<Feature<?>> dungeon = FEATURES.register("dungeon", () -> new MonsterRoomFeature(NoneFeatureConfiguration.CODEC));

    public WorldgenRegistryDesyncTest(IEventBus modEventBus) {
        FEATURES.register(modEventBus);
    }
}
