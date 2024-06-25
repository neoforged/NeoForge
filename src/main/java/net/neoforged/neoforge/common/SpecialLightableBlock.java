/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface SpecialLightableBlock {
    default boolean canLight(BlockState state) {
        return CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state);
    }

    void setLit(LevelAccessor level, BlockState state, BlockPos pos, boolean lit);
}
