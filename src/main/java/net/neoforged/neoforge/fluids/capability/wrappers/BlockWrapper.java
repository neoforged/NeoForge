/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.VoidFluidHandler;

/**
 * Wrapper around any block, only accounts for fluid placement, otherwise the block acts a void.
 */
public class BlockWrapper extends VoidFluidHandler {
    protected final Fluid fluid;
    protected final Level world;
    protected final BlockPos blockPos;

    public BlockWrapper(Fluid fluid, Level world, BlockPos blockPos) {
        this.fluid = fluid;
        this.world = world;
        this.blockPos = blockPos;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // NOTE: "Filling" means placement in this context!
        if (resource.getAmount() < 1000) {
            return 0;
        }
        if (action.execute()) {
            FluidUtil.destroyBlockOnFluidPlacement(world, blockPos);
            BlockState state = fluid.getBlockStateForPlacement(world, blockPos);
            world.setBlock(blockPos, state, Block.UPDATE_ALL_IMMEDIATE);
        }
        return 1000;
    }

    public static class LiquidContainerBlockWrapper extends VoidFluidHandler {
        protected final LiquidBlockContainer liquidContainer;
        protected final Level world;
        protected final BlockPos blockPos;

        public LiquidContainerBlockWrapper(LiquidBlockContainer liquidContainer, Level world, BlockPos blockPos) {
            this.liquidContainer = liquidContainer;
            this.world = world;
            this.blockPos = blockPos;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            // NOTE: "Filling" means placement in this context!
            if (resource.getAmount() >= 1000) {
                BlockState state = world.getBlockState(blockPos);
                if (liquidContainer.canPlaceLiquid(null, world, blockPos, state, resource.getFluid())) {
                    if (action.execute()) {
                        liquidContainer.placeLiquid(world, blockPos, state, resource.getFluid().getStateForPlacement(world, blockPos, resource));
                    }
                    return 1000;
                }
            }
            return 0;
        }
    }
}
