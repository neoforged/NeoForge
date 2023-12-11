/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.wrappers;

import com.google.common.math.IntMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CauldronWrapper implements IFluidHandler {
    private final Level level;
    private final BlockPos pos;

    public CauldronWrapper(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    private CauldronFluidContent getContent(BlockState state) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + pos);
        }
        return content;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent contents = getContent(state);
        return new FluidStack(contents.fluid, contents.totalAmount * contents.currentLevel(state) / contents.maxLevel);
    }

    @Override
    public int getTankCapacity(int tank) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent contents = getContent(state);
        return contents.totalAmount;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return CauldronFluidContent.getForFluid(stack.getFluid()) != null;
    }

    // Called by fill and drain to update the block state.
    private void updateLevel(CauldronFluidContent newContent, int level, FluidAction action) {
        if (action.execute()) {
            BlockState newState = newContent.block.defaultBlockState();

            if (newContent.levelProperty != null) {
                newState = newState.setValue(newContent.levelProperty, level);
            }

            this.level.setBlockAndUpdate(pos, newState);
        }
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }

        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(resource.getFluid());
        if (insertContent == null) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        CauldronFluidContent currentContent = getContent(state);
        if (currentContent.fluid != Fluids.EMPTY && currentContent.fluid != resource.getFluid()) {
            // Fluid mismatch
            return 0;
        }

        // We can only insert increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(insertContent.maxLevel, insertContent.totalAmount);
        int amountIncrements = insertContent.totalAmount / d;
        int levelIncrements = insertContent.maxLevel / d;

        int currentLevel = currentContent.currentLevel(state);
        int insertedIncrements = Math.min(resource.getAmount() / amountIncrements, (insertContent.maxLevel - currentLevel) / levelIncrements);
        if (insertedIncrements > 0) {
            updateLevel(insertContent, currentLevel + insertedIncrements * levelIncrements, action);
        }

        return insertedIncrements * amountIncrements;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }

        BlockState state = level.getBlockState(pos);
        if (getContent(state).fluid == resource.getFluid() && !resource.hasTag()) {
            return drain(state, resource.getAmount(), action);
        } else {
            return FluidStack.EMPTY;
        }
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }

        return drain(level.getBlockState(pos), maxDrain, action);
    }

    private FluidStack drain(BlockState state, int maxDrain, FluidAction action) {
        CauldronFluidContent content = getContent(state);

        // We can only extract increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(content.maxLevel, content.totalAmount);
        int amountIncrements = content.totalAmount / d;
        int levelIncrements = content.maxLevel / d;

        int currentLevel = content.currentLevel(state);
        int extractedIncrements = Math.min(maxDrain / amountIncrements, currentLevel / levelIncrements);
        if (extractedIncrements > 0) {
            int newLevel = currentLevel - extractedIncrements * levelIncrements;
            if (newLevel == 0) {
                // Fully extract -> back to empty cauldron
                if (action.execute()) {
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                }
            } else {
                // Otherwise just decrease levels
                updateLevel(content, newLevel, action);
            }
        }

        return new FluidStack(content.fluid, extractedIncrements * amountIncrements);
    }
}
