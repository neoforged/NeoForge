/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.fluid;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;

public interface FluidTintSource extends BlockTintSource {
    int color(FluidState state);

    default int colorInWorld(FluidState fluidState, BlockState blockState, BlockAndTintGetter level, BlockPos pos) {
        return this.color(fluidState);
    }

    default int colorAsStack(FluidStack stack) {
        return this.color(stack.getFluid().defaultFluidState());
    }

    @Override
    default int color(BlockState state) {
        return this.color(state.getFluidState());
    }

    @Override
    default int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
        return this.colorInWorld(state.getFluidState(), state, level, pos);
    }
}
