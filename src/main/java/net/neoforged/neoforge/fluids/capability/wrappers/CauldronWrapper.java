/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.wrappers;

import com.google.common.collect.MapMaker;
import com.google.common.math.IntMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

@ApiStatus.Internal
public class CauldronWrapper extends SnapshotParticipant<BlockState> implements Storage<FluidVariant> {
    /**
     * To make sure multiple accesses to the same cauldron return the same wrapper,
     * we maintain a {@code (Level, BlockPos) -> Wrapper} cache.</li>
     */
    private record Location(Level level, BlockPos pos) {}

    // Weak values to make sure wrappers are cleaned up after use, thread-safe.
    private static final Map<Location, CauldronWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakValues().makeMap();

    public static CauldronWrapper get(Level level, BlockPos pos) {
        var location = new Location(level, pos.immutable());
        return wrappers.computeIfAbsent(location, CauldronWrapper::new);
    }

    private final Level level;
    private final BlockPos pos;

    private CauldronWrapper(Location location) {
        this.level = location.level;
        this.pos = location.pos;
    }

    @Override
    public int size() {
        return 1;
    }

    private CauldronFluidContent getContent() {
        return getContent(level.getBlockState(pos));
    }

    private CauldronFluidContent getContent(BlockState state) {
        CauldronFluidContent content = CauldronFluidContent.getForBlock(state.getBlock());
        if (content == null) {
            throw new IllegalStateException("Unexpected error: no cauldron at location " + pos);
        }
        return content;
    }

    private static void assertValidTank(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("A cauldron only has one tank. Tried to access slot " + index);
        }
    }

    @Override
    public FluidVariant getResource(int slot) {
        assertValidTank(slot);

        return FluidVariant.of(getContent().fluid);
    }

    @Override
    public boolean isResourceBlank(int slot) {
        return getResource(slot).isBlank();
    }

    @Override
    public long getAmount(int slot) {
        assertValidTank(slot);

        var state = level.getBlockState(pos);
        var contents = getContent(state);
        return (long) contents.totalAmount * contents.currentLevel(state) / contents.maxLevel;
    }

    @Override
    public long getCapacity(int slot, FluidVariant resource) {
        assertValidTank(slot);

        CauldronFluidContent contents;
        if (resource.isBlank()) {
            contents = getContent();
        } else {
            contents = CauldronFluidContent.getForFluid(resource.getFluid());
        }
        if (contents == null) {
            return 0L;
        }
        return contents.totalAmount;
    }

    @Override
    public boolean isValid(int slot, FluidVariant resource) {
        return CauldronFluidContent.getForFluid(resource.getFluid()) != null;
    }

    // Called by fill and drain to update the block state.
    // Note that this temporarily updates the block state in the level
    // See onFinalCommit for where it is modified definitively.
    private void updateLevel(CauldronFluidContent newContent, int level, TransactionContext transaction) {
        updateSnapshots(transaction);

        if (level == 0) {
            // Fully extract -> back to empty cauldron
            this.level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 0);
        } else {
            BlockState newState = newContent.block.defaultBlockState();

            if (newContent.levelProperty != null) {
                newState = newState.setValue(newContent.levelProperty, level);
            }

            this.level.setBlock(pos, newState, 0);
        }
    }

    @Override
    public long insert(int slot, FluidVariant resource, long maxAmount, TransactionContext transaction) {
        assertValidTank(slot);

        return insert(resource, maxAmount, transaction);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank()) {
            return 0;
        }

        CauldronFluidContent insertContent = CauldronFluidContent.getForFluid(resource.getFluid());
        if (insertContent == null) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        CauldronFluidContent currentContent = getContent(state);
        if (currentContent.fluid != Fluids.EMPTY && !resource.is(currentContent.fluid)) {
            // Fluid mismatch
            return 0;
        }

        // We can only insert increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(insertContent.maxLevel, insertContent.totalAmount);
        int amountIncrements = insertContent.totalAmount / d;
        int levelIncrements = insertContent.maxLevel / d;

        int currentLevel = currentContent.currentLevel(state);
        int insertedIncrements = (int) Math.min(maxAmount / amountIncrements, (insertContent.maxLevel - currentLevel) / levelIncrements);
        if (insertedIncrements > 0) {
            updateLevel(insertContent, currentLevel + insertedIncrements * levelIncrements, transaction);
        }

        return (long) insertedIncrements * amountIncrements;
    }

    @Override
    public long extract(int slot, FluidVariant resource, long maxAmount, TransactionContext transaction) {
        assertValidTank(slot);

        return extract(resource, maxAmount, transaction);
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank()) {
            return 0;
        }

        BlockState state = level.getBlockState(pos);
        var content = getContent(state);
        if (resource.is(content.fluid) && resource.getComponents().isEmpty()) {
            return drain(content, state, maxAmount, transaction);
        } else {
            return 0;
        }
    }

    private long drain(CauldronFluidContent content, BlockState state, long maxDrain, TransactionContext transaction) {
        // We can only extract increments based on the GCD between the number of levels and the total amount.
        int d = IntMath.gcd(content.maxLevel, content.totalAmount);
        int amountIncrements = content.totalAmount / d;
        int levelIncrements = content.maxLevel / d;

        int currentLevel = content.currentLevel(state);
        int extractedIncrements = (int) Math.min(maxDrain / amountIncrements, currentLevel / levelIncrements);
        if (extractedIncrements > 0) {
            int newLevel = currentLevel - extractedIncrements * levelIncrements;
            // Otherwise just decrease levels
            updateLevel(content, newLevel, transaction);
        }

        return (long) extractedIncrements * amountIncrements;
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
    protected void onFinalCommit(BlockState originalState) {
        // State as it was modified during this outermost transaction being committed.
        BlockState state = level.getBlockState(pos);

        if (originalState != state) {
            // Revert back to the blockstate before any changes happened so that the next
            // call will not short-circuit due to the blockstate not really changing.
            level.setBlock(pos, originalState, 0);
            // Now do the change that will trigger change notifications to other blocks/neighbors/clients
            level.setBlockAndUpdate(pos, state);
        }
    }
}
