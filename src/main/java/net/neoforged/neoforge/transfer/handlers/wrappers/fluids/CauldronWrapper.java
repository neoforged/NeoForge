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
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A handler for cauldrons. This handler is used to interact with the fluid content of a cauldron.
 */
@ApiStatus.Internal
public final class CauldronWrapper extends SnapshotJournal<BlockState> implements ISingleResourceHandler<FluidResource> {
    // Weak values to make sure wrappers are cleaned up after use, thread-safe.
    private static final Map<WrapperLocation, CauldronWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakKeys().weakValues().makeMap();
    private final WrapperLocation location;

    public static CauldronWrapper get(Level level, BlockPos pos) {
        WrapperLocation location = new WrapperLocation(level, pos.immutable());
        return wrappers.computeIfAbsent(location, CauldronWrapper::new);
    }

    private CauldronWrapper(WrapperLocation location) {
        this.location = location;
    }

    private CauldronFluidContent getContent(BlockState state) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + location.pos + " in " + location.level.dimension().location());
        }
        return content;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());

        BlockState state = location.getBlockState();
        return getContent(state).fluid.getDefaultResource();
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        BlockState state = location.getBlockState();
        return getContent(state).getMillibuckets(state);
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) return CauldronFluidContent.getLargestValue();
        CauldronFluidContent fluidContent = CauldronFluidContent.getForFluid(resource.getInstanceValue());
        return fluidContent == null ? 0 : fluidContent.totalAmount;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty() || CauldronFluidContent.getForFluid(resource.getInstanceValue()) != null;
    }

    @Override
    public int characteristics(int index) {
        return TransferCharacteristics.DEFAULT;
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.DEFAULT;
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
            this.location.level.setBlock(location.pos, Blocks.CAULDRON.defaultBlockState(), 0);
        } else {
            BlockState newState = newContent.block.defaultBlockState();

            if (newContent.levelProperty != null) {
                newState = newState.setValue(newContent.levelProperty, fluidLevel);
            }

            this.location.level.setBlock(location.pos, newState, 0);
        }
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        CauldronFluidContent handledContent = CauldronFluidContent.getForFluid(resource.getInstanceValue());
        if (handledContent == null) {
            return 0;
        }

        BlockState state = location.getBlockState();
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

        BlockState state = location.getBlockState();
        CauldronFluidContent handledContent = getContent(state);

        //This handles the `is(fluid)` as well as components are `empty` with the default resource being the baseline of the fluid
        if (amount < handledContent.getMillibuckets(state) || !resource.equals(handledContent.fluid.getDefaultResource())) {
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
        return location.getBlockState();
    }

    @Override
    protected void revertToSnapshot(BlockState snapshot) {
        location.level.setBlock(location.pos, snapshot, 0);
    }

    @Override
    protected void onCommit(BlockState originalState) {
        // State as it was modified during this outermost transaction being committed.
        BlockState state = location.getBlockState();

        if (originalState == state) return;

        // Revert back to the blockstate before any changes happened so that the next
        // call will not short-circuit due to the blockstate not really changing.
        location.level.setBlock(location.pos, originalState, 0);
        // Now do the change that will trigger change notifications to other blocks/neighbors/clients
        location.level.setBlockAndUpdate(location.pos, state);
    }

    /**
     * Using the location, we can maintain a cache with a given wrapped by using
     * <p>
     * {@code (Level, BlockPos) -> Wrapper}
     */
    private record WrapperLocation(Level level, BlockPos pos) {
        public BlockState getBlockState() {
            return level.getBlockState(pos);
        }
    }
}
