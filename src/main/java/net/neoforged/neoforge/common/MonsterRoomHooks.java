/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = NeoForgeVersion.MOD_ID)
public class MonsterRoomHooks {
    private static final ArrayList<MobEntry> monsterRoomMobs = new ArrayList<>();

    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.ENTITY_TYPE, registry -> {
            monsterRoomMobs.clear();
            registry.getDataMap(NeoForgeDataMaps.MONSTER_ROOM_MOBS).forEach((key, mobData) -> {
                EntityType<?> type = Objects.requireNonNull(registry.get(key), "Nonexistent entity " + key + " in monster room datamap!");
                monsterRoomMobs.add(new MobEntry(type, mobData.weight()));
            });
        });
    }

    /**
     * Gets a random entity type from the weighted list.
     * 
     * @param rand World generation random source
     * @return The entity type
     */
    public static EntityType<?> getRandomDungeonMob(RandomSource rand) {
        MobEntry mob = WeightedRandom.getRandomItem(rand, monsterRoomMobs).orElseThrow();
        return mob.type;
    }

    public record MobEntry(EntityType<?> type, Weight weight) implements WeightedEntry {
        @Override
        public Weight getWeight() {
            return weight;
        }
    }
}
