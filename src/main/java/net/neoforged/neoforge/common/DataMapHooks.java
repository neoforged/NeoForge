/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

// TODO: 1.21.1 remove fallback to vanilla map for waxing and oxidizing
public class DataMapHooks {
    // will be removed in 1.21.1
    /** Used in a gametest */
    @ApiStatus.Internal
    public static boolean didHaveToFallbackToVanillaMaps = false;
    
    /** Mods should not insert anything into this map at all. */
    @ApiStatus.Internal
    private static final Map<Block, Block> INVERSE_OXIDIZABLES_DATAMAP = new HashMap<>();
    /** Mods should not insert anything into this map at all. */
    @ApiStatus.Internal
    private static final Map<Block, Block> INVERSE_WAXABLES_DATAMAP = new HashMap<>();

    /**
     * The inverse map of the oxiziables data map, used in vanilla when scraping oxidization off of a block
     * 
     * @return an inverse map of the {@link NeoForgeDataMaps#OXIDIZABLES oxidizables data map}
     */
    public static Map<Block, Block> getInverseOxidizablesMap() {
        return Collections.unmodifiableMap(INVERSE_OXIDIZABLES_DATAMAP);
    }

    /**
     * The inverse map of the waxables data map, used in vanilla when scraping wax off of a block
     *
     * @return an inverse map of the {@link NeoForgeDataMaps#WAXABLES waxables data map}
     */
    public static Map<Block, Block> getInverseWaxablesMap() {
        return Collections.unmodifiableMap(INVERSE_WAXABLES_DATAMAP);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Block getNextOxidizedStage(Block block) {
        Oxidizable oxidizable = block.builtInRegistryHolder().getData(NeoForgeDataMaps.OXIDIZABLES);
        return oxidizable != null ? oxidizable.nextOxidizedStage() : WeatheringCopper.NEXT_BY_BLOCK.get().get(block);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Block getPreviousOxidizedStage(Block block) {
        return getInverseOxidizablesMap().containsKey(block) ? getInverseOxidizablesMap().get(block) : WeatheringCopper.PREVIOUS_BY_BLOCK.get().get(block);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Block getBlockWaxed(Block block) {
        Waxable waxable = block.builtInRegistryHolder().getData(NeoForgeDataMaps.WAXABLES);
        return waxable != null ? waxable.waxed() : HoneycombItem.WAXABLES.get().get(block);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Block getBlockUnwaxed(Block block) {
        return getInverseWaxablesMap().containsKey(block) ? getInverseWaxablesMap().get(block) : HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block);
    }

    @SubscribeEvent
    static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.BLOCK, registry -> {
            INVERSE_OXIDIZABLES_DATAMAP.clear();
            INVERSE_WAXABLES_DATAMAP.clear();
            
            registry.getDataMap(NeoForgeDataMaps.OXIDIZABLES).forEach((resourceKey, oxidizable) -> {
                INVERSE_OXIDIZABLES_DATAMAP.put(oxidizable.nextOxidizedStage(), BuiltInRegistries.BLOCK.get(resourceKey));
            });

            //noinspection deprecation
            WeatheringCopper.PREVIOUS_BY_BLOCK.get().forEach((after, before) -> {
                if (!INVERSE_OXIDIZABLES_DATAMAP.containsKey(after)) {
                    INVERSE_OXIDIZABLES_DATAMAP.put(after, before);
                    didHaveToFallbackToVanillaMaps = true;
                }
            });

            registry.getDataMap(NeoForgeDataMaps.WAXABLES).forEach((resourceKey, waxable) -> {
                INVERSE_WAXABLES_DATAMAP.put(waxable.waxed(), BuiltInRegistries.BLOCK.get(resourceKey));
            });

            //noinspection deprecation
            HoneycombItem.WAX_OFF_BY_BLOCK.get().forEach((after, before) -> {
                if (!INVERSE_WAXABLES_DATAMAP.containsKey(after)) {
                    INVERSE_OXIDIZABLES_DATAMAP.put(after, before);
                    didHaveToFallbackToVanillaMaps = true;
                }
            });
        });
    }
}
