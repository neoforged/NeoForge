/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
public class MonsterRoomHooks {
    private static WeightedList<EntityType<?>> monsterRoomMobs = WeightedList.of();

    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.ENTITY_TYPE, registry -> monsterRoomMobs = WeightedList.of(registry.getDataMap(NeoForgeDataMaps.MONSTER_ROOM_MOBS).entrySet().stream().map((entry) -> {
            EntityType<?> type = Objects.requireNonNull(registry.getValue(entry.getKey()), "Nonexistent entity " + entry.getKey() + " in monster room datamap!");
            return new Weighted<EntityType<?>>(type, entry.getValue().weight());
        }).toList()));
    }

    /**
     * Gets a random entity type from the weighted list.
     * 
     * @param rand World generation random source
     * @return The entity type
     */
    public static EntityType<?> getRandomMonsterRoomMob(RandomSource rand) {
        return monsterRoomMobs.getRandomOrThrow(rand);
    }
}
