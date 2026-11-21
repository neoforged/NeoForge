/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.common.crafting.RecipePropertySetManager;
import net.neoforged.neoforge.common.tooltip.ItemTooltipHandler;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@ApiStatus.Internal
public class GameData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");

    public static Map<Block, Item> getBlockItemMap() {
        return NeoForgeRegistryCallbacks.ItemCallbacks.BLOCK_TO_ITEM_MAP;
    }

    public static IdMapper<BlockState> getBlockStateIDMap() {
        return NeoForgeRegistryCallbacks.BlockCallbacks.BLOCKSTATE_TO_ID_MAP;
    }

    public static Map<BlockState, Holder<PoiType>> getBlockStatePointOfInterestTypeMap() {
        return NeoForgeRegistryCallbacks.PoiTypeCallbacks.BLOCKSTATE_TO_POI_TYPE_MAP;
    }

    public static Table<Block, Block, Block> getFlowerPotBlockTable() {
        return NeoForgeRegistryCallbacks.BlockCallbacks.EMPTY_POT_AND_FLOWER_TO_FULL_POT_TABLE;
    }

    public static void vanillaSnapshot() {
        LOGGER.debug(REGISTRIES, "Creating vanilla freeze snapshot");
        RegistryManager.takeVanillaSnapshot();
        LOGGER.debug(REGISTRIES, "Vanilla freeze snapshot created");
    }

    public static void unfreezeData() {
        LOGGER.debug(REGISTRIES, "Unfreezing registries");
        RegistryManager.unfreezeRegistries(BuiltInRegistries.REGISTRY);
    }

    public static void freezeData() {
        LOGGER.debug(REGISTRIES, "Freezing registries");
        RegistryManager.freezeRegistries(BuiltInRegistries.REGISTRY);
        RegistryManager.takeFrozenSnapshot();
        LOGGER.debug(REGISTRIES, "All registries frozen");
    }

    public static void runPostRegistrationTasks() {
        CommonHooks.modifyAttributes();
        SpawnPlacements.fireSpawnPlacementEvent();
        ModLoader.postEvent(new BlockEntityTypeAddBlocksEvent());
        CreativeModeTabRegistry.sortTabs();
        GameRuleCategory.registerModdedCategories();
        ItemTooltipHandler.init();
        RecipePropertySetManager.init();
    }
}
