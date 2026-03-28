/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Additional helper methods for {@link CropBlock}. Applicable for other blocks supporting harvesting
 */
public interface IHarvestable {
    /**
     * Returns list of harvest result for harvesting the crop. Make sure to call
     * {@link CropBlock#updateCropAfterHarvest(ServerLevel, RandomSource, BlockPos, BlockState)}
     * after realizing the result.
     *
     * @return List of loot when harvested. Return empty list if the crop is not ready to be harvested. Return block loot by default.
     */
    default java.util.List<ItemStack> getHarvestResult(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool) {
        net.minecraft.world.level.storage.loot.LootParams.Builder params = new net.minecraft.world.level.storage.loot.LootParams.Builder(level)
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.ORIGIN, net.minecraft.world.phys.Vec3.atCenterOf(pos))
                .withParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.TOOL, tool)
                .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.THIS_ENTITY, user)
                .withOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));
        return state.getDrops(params);
    }

    /**
     * Modify the crop after harvesting.
     *
     * @return true if custom post-harvesting logic is executed. Otherwise caller should use its own harvest logic.
     */
    default boolean updateCropAfterHarvest(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        return false;
    }
}
