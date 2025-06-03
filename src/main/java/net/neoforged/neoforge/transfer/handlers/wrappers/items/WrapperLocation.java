/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Using the location, we can maintain a cache with a given wrapped by using
 * <p>
 * {@code (Level, BlockPos) -> Wrapper}
 */
public record WrapperLocation(Level level, BlockPos pos) {
    public BlockState getBlockState() {
        return level.getBlockState(pos);
    }
}
