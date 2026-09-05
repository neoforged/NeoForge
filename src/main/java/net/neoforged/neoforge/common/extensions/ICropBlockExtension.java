/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IHarvestable;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("deprecation")
public interface ICropBlockExtension extends IHarvestable {
    private CropBlock self() {
        return (CropBlock) this;
    }

    /// state sensitive variant of [CropBlock#getStateForAge(int)]
    ///
    /// Override if the crop has block state properties other than age.
    default BlockState getStateForAge(LevelReader level, BlockPos pos, BlockState state, int age) {
        return self().getStateForAge(age);
    }

    default List<ItemStack> getHarvestResult(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, @Nullable Entity user, ItemStack tool) {
        if (self().getAge(state) < self().getMaxAge())
            return List.of();
        return Block.getDrops(state, level, pos, level.getBlockEntity(pos), user, tool);
    }
}
