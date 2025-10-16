/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A handler for cauldrons. This handler is used to interact with the fluid content of a cauldron.
 */
@ApiStatus.Internal
public final class CauldronWrapper extends SnapshotJournal<BlockState> implements ResourceHandler<FluidResource> {
    /**
     * To make sure multiple accesses to the same cauldron return the same wrapper,
     * we maintain a {@code (Level, BlockPos) -> Wrapper} cache.
     */
    private record WrapperLocation(Level level, BlockPos pos) {
        public BlockState getBlockState() {
            return level.getBlockState(pos);
        }
    }

    /**
     * Wrapper map, similar to {@link VanillaContainerWrapper#wrappers}.
     * We need the cauldron wrapper to hold a strong reference to the wrapper location to avoid the weak keys being cleared too early.
     */
    private static final Map<WrapperLocation, CauldronWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakKeys().weakValues().makeMap();

    public static CauldronWrapper get(Level level, BlockPos pos) {
        WrapperLocation location = new WrapperLocation(level, pos.immutable());
        return wrappers.computeIfAbsent(location, CauldronWrapper::new);
    }

    private final WrapperLocation location;

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
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());

        BlockState state = location.getBlockState();
        return FluidResource.of(getContent(state).fluid);
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());

        BlockState state = location.getBlockState();
        CauldronFluidContent content = getContent(state);
        // Note that non-integer amounts (such as 1000/3 for example) are rounded down.
        return (long) content.totalAmount * content.currentLevel(state) / content.maxLevel;
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, size());

        // Note that the empty fluid has a content registered for it with a capacity of 1 bucket, so this case does not require special handling.
        // TODO: For the empty fluid, consider returning the largest capacity across all registered fluid contents instead.
        CauldronFluidContent fluidContent = CauldronFluidContent.getForFluid(resource.getFluid());
        return fluidContent == null ? 0 : fluidContent.totalAmount;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmpty(resource);

        return resource.isComponentsPatchEmpty() && CauldronFluidContent.getForFluid(resource.getFluid()) != null;
    }

    /**
     * Temporarily updates the block state in the level in a transactional way.
     * See {@link #onRootCommit} for the final modification.
     */
    private void setLevel(CauldronFluidContent newContent, int fluidLevel, TransactionContext transaction) {
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
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        if (!resource.isComponentsPatchEmpty()) {
            // Don't accept patched resources as we can only represent the Fluid in a cauldron.
            return 0;
        }
        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(resource.getFluid());
        if (insertContent == null) {
            return 0;
        }

        BlockState state = location.getBlockState();
        CauldronFluidContent currentContent = getContent(state);
        if (currentContent.fluid != Fluids.EMPTY && !resource.is(currentContent.fluid)) {
            // Fluid in the cauldron does not match the input
            return 0;
        }

        // We can only insert increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(insertContent.maxLevel, insertContent.totalAmount);
        int amountIncrements = insertContent.totalAmount / d;
        int levelIncrements = insertContent.maxLevel / d;

        int currentLevel = currentContent.currentLevel(state);
        int insertedIncrements = Math.min(amount / amountIncrements, (insertContent.maxLevel - currentLevel) / levelIncrements);

        if (insertedIncrements > 0) {
            setLevel(insertContent, currentLevel + insertedIncrements * levelIncrements, transaction);
        }

        return insertedIncrements * amountIncrements;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        BlockState state = location.getBlockState();
        CauldronFluidContent currentContent = getContent(state);

        if (!resource.is(currentContent.fluid) || !resource.isComponentsPatchEmpty()) {
            return 0;
        }

        // We can only extract increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(currentContent.maxLevel, currentContent.totalAmount);
        int levelIncrements = currentContent.maxLevel / d;
        int amountIncrements = currentContent.totalAmount / d;

        int currentLevel = currentContent.currentLevel(state);
        int extractedIncrements = Math.min(amount / amountIncrements, currentLevel / levelIncrements);

        if (extractedIncrements > 0) {
            setLevel(currentContent, currentLevel - extractedIncrements * levelIncrements, transaction);
        }

        return extractedIncrements * amountIncrements;
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
    protected void onRootCommit(BlockState originalState) {
        BlockState state = location.getBlockState();

        // Skip updating if nothing changed or if the cauldron was removed
        if (originalState == state || CauldronFluidContent.getForBlock(state.getBlock()) == null) return;

        // Revert back to the blockstate before any changes happened so that the next
        // call will not short-circuit due to the blockstate not really changing.
        location.level.setBlock(location.pos, originalState, 0);
        // Now perform the change that will trigger notifications to other blocks/neighbors/clients.
        location.level.setBlockAndUpdate(location.pos, state);

        // Currently we don't send a BLOCK_CHANGE nor FLUID_PLACE/FLUID_PICKUP game event. This can be reconsidered.
    }
}
