/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

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
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class ComposterWrapper extends SnapshotJournal<Float> {
    private static final ItemResource BONE_MEAL = ItemResource.of(Items.BONE_MEAL);

    //Floats to avoid boxing and unboxing when taking a snapshot or assigning;
    //Also allows us to make use of the Float.equals
    private static final Float NO_OP = 0f;
    private static final Float EXTRACT = -1f;

    private final WrapperLocation location;
    // -1 if bonemeal was extracted, otherwise the composter increase probability of the (pending) inserted item.
    private Float probability = NO_OP;

    private final IResourceHandler<ItemResource> topHandler = new Top();
    private final IResourceHandler<ItemResource> bottomHandler = new Bottom();

    // Weak values to make sure wrappers are cleaned up after use, thread-safe.
    private static final Map<WrapperLocation, ComposterWrapper> wrappers = new MapMaker().concurrencyLevel(1).weakValues().makeMap();

    @Nullable
    public static IResourceHandler<ItemResource> get(Level level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null || !direction.getAxis().isVertical()) return null;

        WrapperLocation location = new WrapperLocation(level, pos.immutable());
        ComposterWrapper wrapper = wrappers.computeIfAbsent(location, ComposterWrapper::new);
        return direction == Direction.UP ? wrapper.topHandler : wrapper.bottomHandler;
    }

    private ComposterWrapper(WrapperLocation location) {
        this.location = location;
    }

    @Override
    protected Float createSnapshot() {
        return probability;
    }

    @Override
    protected void revertToSnapshot(Float snapshot) {
        probability = snapshot;
    }

    @Override
    protected void onCommit(Float originalState) {
        if (probability.equals(NO_OP)) return;

        // Apply pending action
        if (probability.equals(EXTRACT)) {
            // Mimic ComposterBlock#empty logic.
            BlockState newState = location.blockstate().setValue(ComposterBlock.LEVEL, ComposterBlock.MIN_LEVEL);
            location.level.setBlockAndUpdate(location.pos, newState);
            location.level.gameEvent(GameEvent.BLOCK_CHANGE, location.pos, GameEvent.Context.of(null, newState));
        } else {
            BlockState state = location.blockstate();
            // Always increment on first insert (like vanilla).
            boolean increaseSuccessful = state.getValue(ComposterBlock.LEVEL) == ComposterBlock.MIN_LEVEL || location.level.getRandom().nextDouble() < probability;

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
        probability = NO_OP;
    }

    private boolean isBoneMeal(ItemResource resource) {
        return resource.equals(BONE_MEAL);
    }

    private boolean hasBoneMeal() {
        // We only have bone meal if the level is READY and no action was scheduled.
        return probability.equals(NO_OP) && location.blockstate().getValue(ComposterBlock.LEVEL) == ComposterBlock.READY;
    }

    private float getValueFrom(ItemResource resource) {
        return ComposterBlock.getValue(resource.toStack());
    }

    private class Top implements ISingleResourceHandler<ItemResource> {
        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

            // Check that no action is scheduled.
            if (!probability.equals(NO_OP)) return 0;
            // Check that the composter can accept items.
            if (location.blockstate().getValue(ComposterBlock.LEVEL) >= ComposterBlock.READY) return 0;

            // Check that the item is compostable.
            float insertedIncreaseProbability = getValueFrom(resource);
            if (insertedIncreaseProbability <= 0) return 0;

            // Schedule insertion.
            updateSnapshots(transaction);
            probability += insertedIncreaseProbability;
            return 1;
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public boolean supportsInsertion() {
            return true;
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.EMPTY;
        }

        @Override
        public int getAmount(int index) {
            return 0;
        }

        @Override
        public int getCapacity(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || getValueFrom(resource) > 0 ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return getValueFrom(resource) > 0;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/top]";
        }
    }

    private class Bottom implements ISingleResourceHandler<ItemResource> {
        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

            // Check that the resource is bone meal & there is bone meal to extract.
            if (!isBoneMeal(resource) || !hasBoneMeal()) return 0;

            updateSnapshots(transaction);
            probability = EXTRACT;
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            Objects.checkIndex(index, size());
            return hasBoneMeal() ? BONE_MEAL : ItemResource.EMPTY;
        }

        @Override
        public int getAmount(int index) {
            Objects.checkIndex(index, size());
            return hasBoneMeal() ? 1 : 0;
        }

        @Override
        public int getCapacity(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return resource.isEmpty() || isBoneMeal(resource) ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return false;
        }

        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public boolean supportsExtraction() {
            return true;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/bottom]";
        }
    }

    /**
     * Using the location, we can maintain a cache with a given wrapped by using
     * <p>
     * {@code (Level, BlockPos) -> Wrapper}
     */
    private record WrapperLocation(Level level, BlockPos pos) {
        public BlockState blockstate() {
            return level.getBlockState(pos);
        }
    }
}
