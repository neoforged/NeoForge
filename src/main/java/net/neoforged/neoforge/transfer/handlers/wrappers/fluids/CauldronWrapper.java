/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import com.google.common.collect.MapMaker;
import com.google.common.math.IntMath;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.WrapperLocation;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A handler for cauldrons. This handler is used to interact with the fluid content of a cauldron.
 */
public class CauldronWrapper extends SnapshotJournal<BlockState> implements ISingleResourceHandler<FluidResource> {
    // Weak values to make sure wrappers are cleaned up after use, thread-safe.
    private static final Map<WrapperLocation, CauldronWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakValues().makeMap();
    private final Level level;
    private final BlockPos pos;

    public static CauldronWrapper get(Level level, BlockPos pos) {
        var location = new WrapperLocation(level, pos.immutable());
        return wrappers.computeIfAbsent(location, CauldronWrapper::new);
    }

    private CauldronWrapper(WrapperLocation location) {
        this.level = location.level();
        this.pos = location.pos();
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
        Objects.checkIndex(index, size());

        BlockState state = level.getBlockState(pos);
        return getContent(state).fluid.defaultResource();
    }

    @Override
    public int getAmount(int index) {
        BlockState state = level.getBlockState(pos);
        return getContent(state).getMillibuckets(state);
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        CauldronFluidContent fluidContent = CauldronFluidContent.getForFluid(resource.getInstanceValue());
        return fluidContent == null ? 0 : fluidContent.totalAmount;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return CauldronFluidContent.getForFluid(resource.getInstanceValue()) != null;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    /**
     * Called by fill and drain to update the block state.
     * Note that this temporarily updates the block state in the level
     *
     * @see #onCommit
     */
    private void updateSnapshotAndSetBlock(CauldronFluidContent newContent, int fluidLevel, TransactionContext transaction) {
        updateSnapshots(transaction);

        if (fluidLevel == 0) {
            // Fully extract -> back to empty cauldron
            this.level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 0);
        } else {
            BlockState newState = newContent.block.defaultBlockState();

            if (newContent.levelProperty != null) {
                newState = newState.setValue(newContent.levelProperty, fluidLevel);
            }

            this.level.setBlock(pos, newState, 0);
        }
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        CauldronFluidContent handledContent = CauldronFluidContent.getForFluid(resource.getInstanceValue());
        if (handledContent == null) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        CauldronFluidContent currentContent = getContent(state);

        if (currentContent.fluid != Fluids.EMPTY && !resource.is(currentContent.fluid)) {
            //Fluid in the cauldron does not match our input
            return 0;
        }

        int currentLevel = currentContent.currentLevel(state);

        // We can only insert increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(handledContent.maxLevel, handledContent.totalAmount);
        int amountIncrements = handledContent.totalAmount / d;
        int levelIncrements = handledContent.maxLevel / d;
        int handledIncrements = Math.min(amount / amountIncrements, (handledContent.maxLevel - currentLevel) / levelIncrements);

        if (handledIncrements > 0) {
            updateSnapshotAndSetBlock(handledContent, currentLevel + handledIncrements * levelIncrements, transaction);
        }

        return handledIncrements * amountIncrements;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        BlockState state = level.getBlockState(pos);
        CauldronFluidContent handledContent = getContent(state);

        //This handles the `is(fluid)` as well as components are `empty` with the default resource being the baseline of the fluid
        if (amount < handledContent.getMillibuckets(state) || !resource.equals(handledContent.fluid.defaultResource())) {
            return 0;
        }

        int currentLevel = handledContent.currentLevel(state);

        // We can only extract increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(handledContent.maxLevel, handledContent.totalAmount);
        int levelIncrements = handledContent.maxLevel / d;
        int amountIncrements = handledContent.totalAmount / d;
        int handledIncrements = Math.min(amount / amountIncrements, currentLevel / levelIncrements);

        if (handledIncrements > 0) {
            updateSnapshotAndSetBlock(handledContent, currentLevel - handledIncrements * levelIncrements, transaction);
        }

        return handledIncrements * amountIncrements;
    }

    @Override
    protected BlockState createSnapshot() {
        return level.getBlockState(pos);
    }

    @Override
    protected void revertToSnapshot(BlockState snapshot) {
        level.setBlock(pos, snapshot, 0);
    }

    @Override
    protected void onCommit(BlockState originalState) {
        // State as it was modified during this outermost transaction being committed.
        BlockState state = level.getBlockState(pos);

        if (originalState == state) return;

        // Revert back to the blockstate before any changes happened so that the next
        // call will not short-circuit due to the blockstate not really changing.
        level.setBlock(pos, originalState, 0);
        // Now do the change that will trigger change notifications to other blocks/neighbors/clients
        level.setBlockAndUpdate(pos, state);
    }
}
