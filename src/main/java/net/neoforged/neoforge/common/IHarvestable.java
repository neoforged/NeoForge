/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/// This interface allow blocks to define custom harvest results and post-harvest block state changes.
public interface IHarvestable {
    /// Returns list of harvest result for harvesting the crop. Make sure to call
    /// [#updateCropAfterHarvest(ServerLevel,RandomSource,BlockPos,BlockState)]
    /// after harvesting.
    ///
    /// @return List of loot when harvested. Return empty list if the crop is not ready to be harvested. Return block loot by default.
    default List<ItemStack> getHarvestResult(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool) {
        if (this instanceof CropBlock crop) {
            if (crop.getAge(state) < crop.getMaxAge())
                return java.util.List.of();
        }
        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, user)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));
        return state.getDrops(params);
    }

    /// Modify the crop after harvesting.
    ///
    /// @return true if custom post-harvesting logic is executed. Otherwise, caller should use its own harvest logic,
    /// such as removing the block or setting it to age 1 if it's a crop block.
    default boolean updateCropAfterHarvest(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }
}
