/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.HashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, modid = NeoForgeVersion.MOD_ID)
public class DataMapHooks {
    public static final HashMap<Block, Block> INVERSE_OXIDIZABLES_DATAMAP = new HashMap<>();
    public static final HashMap<Block, Block> INVERSE_WAXABLES_DATAMAP = new HashMap<>();

    @SubscribeEvent
    public static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.BLOCK, registry -> {
            registry.getDataMap(NeoForgeDataMaps.OXIDIZING_BLOCKS).forEach((resourceKey, oxidizable) -> {
                INVERSE_OXIDIZABLES_DATAMAP.clear();
                INVERSE_OXIDIZABLES_DATAMAP.put(oxidizable.after(), BuiltInRegistries.BLOCK.get(resourceKey));

                //noinspection deprecation
                WeatheringCopper.PREVIOUS_BY_BLOCK.get().forEach((after, before) -> {
                    if (!INVERSE_WAXABLES_DATAMAP.containsKey(after))
                        INVERSE_OXIDIZABLES_DATAMAP.put(after, before);
                });
            });

            registry.getDataMap(NeoForgeDataMaps.WAXABLE_BLOCKS).forEach((resourceKey, waxable) -> {
                INVERSE_WAXABLES_DATAMAP.clear();
                INVERSE_WAXABLES_DATAMAP.put(waxable.after(), BuiltInRegistries.BLOCK.get(resourceKey));

                //noinspection deprecation
                HoneycombItem.WAX_OFF_BY_BLOCK.get().forEach((after, before) -> {
                    if (!INVERSE_WAXABLES_DATAMAP.containsKey(after))
                        INVERSE_OXIDIZABLES_DATAMAP.put(after, before);
                });
            });
        });
    }
}
