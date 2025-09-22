/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.Map;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * {@code ResourceHandler<ItemResource>} implementation for the composter block.
 */
@ApiStatus.Internal
public class ComposterWrapper extends SnapshotJournal<Float> {
    /**
     * To make sure multiple accesses to the same composter return the same wrapper,
     * we maintain a {@code (Level, BlockPos) -> Wrapper} cache.
     */
    private record WrapperLocation(Level level, BlockPos pos) {
        public BlockState getBlockState() {
            return level.getBlockState(pos);
        }
    }

    /**
     * Wrapper map, similar to {@link VanillaContainerWrapper#wrappers}.
     * We need the composter wrapper to hold a strong reference to the wrapper location to avoid the weak key being cleared too early,
     * and we need each sub-handler to hold a strong reference to the composter wrapper to avoid the weak value being cleared too early.
     */
    private static final Map<WrapperLocation, ComposterWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakKeys().weakValues().makeMap();

    @Nullable
    public static ResourceHandler<ItemResource> get(Level level, BlockPos pos, @Nullable Direction direction) {
        // TODO: for a null direction we could return a read-only view of the bottom handler
        if (direction == null || !direction.getAxis().isVertical()) return null;

        WrapperLocation location = new WrapperLocation(level, pos.immutable());
        ComposterWrapper wrapper = wrappers.computeIfAbsent(location, ComposterWrapper::new);
        return direction == Direction.UP ? wrapper.topHandler : wrapper.bottomHandler;
    }

    private static final ItemResource BONE_MEAL = ItemResource.of(Items.BONE_MEAL);

    // Floats to avoid boxing and unboxing when taking a snapshot or assigning.
    private static final Float DO_NOTHING = 0f;
    private static final Float EXTRACT_BONEMEAL = -1f;

    private final WrapperLocation location;
    // -1 if bonemeal was extracted, otherwise the composter increase probability of the (pending) inserted item.
    private Float increaseProbability = DO_NOTHING;
    private final ResourceHandler<ItemResource> topHandler = new Top();
    private final ResourceHandler<ItemResource> bottomHandler = new Bottom();

    private ComposterWrapper(WrapperLocation location) {
        this.location = location;
    }

    @Override
    protected Float createSnapshot() {
        return increaseProbability;
    }

    @Override
    protected void revertToSnapshot(Float snapshot) {
        increaseProbability = snapshot;
    }

    @Override
    protected void onRootCommit(Float originalState) {
        // Apply pending action
        if (increaseProbability.equals(EXTRACT_BONEMEAL)) {
            // Mimic ComposterBlock#empty logic.
            BlockState newState = location.getBlockState().setValue(ComposterBlock.LEVEL, ComposterBlock.MIN_LEVEL);
            location.level.setBlockAndUpdate(location.pos, newState);
            location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(null, newState));
        } else {
            BlockState state = location.getBlockState();
            // Always increment on first insert (like vanilla).
            boolean increaseSuccessful = state.getValue(ComposterBlock.LEVEL) == ComposterBlock.MIN_LEVEL || location.level.getRandom().nextDouble() < increaseProbability;

            if (increaseSuccessful) {
                // Mimic ComposterBlock#addItem logic.
                int newLevel = state.getValue(ComposterBlock.LEVEL) + 1;
                BlockState newState = state.setValue(ComposterBlock.LEVEL, newLevel);
                location.level.setBlockAndUpdate(location.pos, newState);
                location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(null, newState));

                if (newLevel == ComposterBlock.MAX_LEVEL) {
                    location.level.scheduleTick(location.pos, state.getBlock(), SharedConstants.TICKS_PER_SECOND);
                }
            }

            location.level.levelEvent(LevelEvent.COMPOSTER_FILL, location.pos, increaseSuccessful ? 1 : 0);
        }

        // Reset after successful commit.
        increaseProbability = DO_NOTHING;
    }

    private boolean hasBoneMeal() {
        // We only have bone meal if the level is READY and no action was scheduled.
        return increaseProbability.equals(DO_NOTHING) && location.getBlockState().getValue(ComposterBlock.LEVEL) == ComposterBlock.READY;
    }

    private static float getComposterValue(ItemResource resource) {
        return ComposterBlock.getValue(resource.toStack());
    }

    private class Top implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            // Check amount.
            if (amount < 1) return 0;
            // Check that no action is scheduled.
            if (!increaseProbability.equals(DO_NOTHING)) return 0;
            // Check that the composter can accept items.
            if (location.getBlockState().getValue(ComposterBlock.LEVEL) >= ComposterBlock.MAX_LEVEL) return 0;
            // Check that the item is compostable.
            float insertedIncreaseProbability = getComposterValue(resource);
            if (insertedIncreaseProbability <= 0) return 0;

            // Schedule insertion.
            updateSnapshots(transaction);
            increaseProbability = insertedIncreaseProbability;
            return 1;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            return 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || getComposterValue(resource) > 0 ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return getComposterValue(resource) > 0;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/top]";
        }
    }

    private class Bottom implements ResourceHandler<ItemResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(index, size());
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            // Check amount.
            if (amount < 1) return 0;
            // Check that the resource is bone meal.
            if (!BONE_MEAL.equals(resource)) return 0;
            // Check that there is bone meal to extract.
            if (!hasBoneMeal()) return 0;

            updateSnapshots(transaction);
            increaseProbability = EXTRACT_BONEMEAL;
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            Objects.checkIndex(index, size());
            return hasBoneMeal() ? BONE_MEAL : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index) {
            Objects.checkIndex(index, size());
            return hasBoneMeal() ? 1 : 0;
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || BONE_MEAL.equals(resource) ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return BONE_MEAL.equals(resource);
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/bottom]";
        }
    }
}
