/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import com.google.common.math.IntMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.ISingleResourceHandler;

/**
 * A handler for cauldrons. This handler is used to interact with the fluid content of a cauldron.
 */
public class CauldronHandler implements ISingleResourceHandler<FluidResource> {
    private final Level level;
    private final BlockPos pos;

    public CauldronHandler(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    private CauldronFluidContent getContent(BlockState state) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + pos);
        }
        return content;
    }

    @Override
    public FluidResource getResource(int index) {
        BlockState state = level.getBlockState(pos);
        return getContent(state).fluid.defaultResource;
    }

    @Override
    public int getAmount(int index) {
        BlockState state = level.getBlockState(pos);
        return getContent(state).getMillibuckets(state);
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        CauldronFluidContent fluidContent = CauldronFluidContent.getForFluid(resource.getFluid());
        return fluidContent == null ? 0 : fluidContent.totalAmount;
    }

    @Override
    public int getCapacity(int index) {
        //We could probably have something that queries all of the totalAmounts for every fluid at startup, and have that;
        // but 1 bucket seems more reasonable as a theoretical than MaxInt
        return FluidType.BUCKET_VOLUME;
        //        return Integer.MAX_VALUE; // CauldronFluidContent.totalAmount does not have a maximum value
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return CauldronFluidContent.getForFluid(resource.getFluid()) != null;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    private void updateLevel(CauldronFluidContent newContent, int level) {
        BlockState newState = newContent.block.defaultBlockState();

        if (newContent.levelProperty != null) {
            newState = newState.setValue(newContent.levelProperty, level);
        }

        this.level.setBlockAndUpdate(pos, newState);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent currentContent = getContent(state);

        if (resource.isEmpty() || amount <= 0) {
            return 0;
        } else if (currentContent.fluid != Fluids.EMPTY && !resource.equals(currentContent.fluid.defaultResource)) {
            return 0;
        }

        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(resource.getFluid());
        if (insertContent == null) {
            return 0;
        }


        // We can only insert increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(insertContent.maxLevel, insertContent.totalAmount);
        int amountIncrements = insertContent.totalAmount / d;
        int levelIncrements = insertContent.maxLevel / d;

        int currentLevel = currentContent.currentLevel(state);
        int insertedIncrements = Math.min(amount / amountIncrements, (insertContent.maxLevel - currentLevel) / levelIncrements);
        if (insertedIncrements > 0 && action.isExecuting()) {
            updateLevel(insertContent, currentLevel + insertedIncrements * levelIncrements);
        }

        return insertedIncrements * amountIncrements;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        BlockState state = level.getBlockState(pos);
        CauldronFluidContent content = getContent(state);
        if (amount < content.getMillibuckets(state) || resource.isEmpty() || !resource.equals(content.fluid.defaultResource)) {
            return 0;
        }

        int d = IntMath.gcd(content.maxLevel, content.totalAmount);
        int amountIncrements = content.totalAmount / d;
        int levelIncrements = content.maxLevel / d;

        int currentLevel = content.currentLevel(state);
        int extractedIncrements = Math.min(amount / amountIncrements, currentLevel / levelIncrements);
        if (extractedIncrements > 0) {
            int newLevel = currentLevel - extractedIncrements * levelIncrements;
            if (action.isExecuting()) {
                if (newLevel == 0) {
                    // Fully extract -> back to empty cauldron
                    level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                } else {
                    // Otherwise just decrease levels
                    updateLevel(content, newLevel);
                }
            }
        }

        return extractedIncrements * amountIncrements;
    }

}
