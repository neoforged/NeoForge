/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.fluid;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface CustomFluidRenderer {
    /// Called to allow rendering custom quads for a fluid during chunk meshing. You may replace the fluid
    /// rendering entirely, or return false to allow vanilla's fluid rendering to also run.
    ///
    /// Note: this method will be called once for every fluid block during chunk meshing, so any logic
    /// here needs to be performant.
    ///
    /// @param fluidRenderer the vanilla fluid renderer
    /// @param fluidState    the state of the fluid
    /// @param getter        the getter the fluid can be obtained from
    /// @param pos           the position of the fluid
    /// @param output        the [FluidRenderer.Output] to get vertex consumers from
    /// @param blockState    the blockstate at the position of the fluid
    /// @return true if vanilla fluid rendering should be skipped
    boolean renderFluid(FluidRenderer fluidRenderer, FluidState fluidState, BlockAndTintGetter getter, BlockPos pos, FluidRenderer.Output output, BlockState blockState);
}
