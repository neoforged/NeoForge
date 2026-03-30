/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jspecify.annotations.Nullable;

/// This interface allow blocks to define custom harvest results and post-harvest block state changes.
public interface IHarvestable {
    /// helper method to get harvest result from interaction loot table
    static List<ItemStack> getHarvestResultFromInteractionLootTable(ServerLevel level, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool, ResourceKey<LootTable> key) {
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.BLOCK_STATE, state)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos))
                .withOptionalParameter(LootContextParams.INTERACTING_ENTITY, user)
                .withOptionalParameter(LootContextParams.TOOL, tool)
                .create(LootContextParamSets.BLOCK_INTERACT);
        return lootTable.getRandomItems(params);
    }

    /// Returns list of harvest result for harvesting the crop. Make sure to call
    /// [#updateCropAfterHarvest(ServerLevel,RandomSource,BlockPos,BlockState)]
    /// after harvesting.
    ///
    /// @return List of loot when harvested. Return empty list if the crop is not ready to be harvested. Return block loot by default.
    default List<ItemStack> getHarvestResult(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool) {
        if (this instanceof CropBlock crop) {
            if (crop.getAge(state) < crop.getMaxAge())
                return List.of();
        }
        return Block.getDrops(state, level, pos, level.getBlockEntity(pos), user, tool);
    }

    /// Modify the crop after harvesting.
    ///
    /// @return true if custom post-harvesting logic is executed. Otherwise, caller should use its own harvest logic,
    /// such as removing the block or setting it to age 1 if it's a crop block.
    default boolean updateCropAfterHarvest(ServerLevel level, RandomSource random, BlockPos pos, @Nullable Entity user, BlockState state) {
        return false;
    }
}
