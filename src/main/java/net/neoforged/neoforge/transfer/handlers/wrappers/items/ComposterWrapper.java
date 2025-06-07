/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import com.google.common.collect.MapMaker;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class ComposterWrapper extends SnapshotJournal<Float> {
    private static final float NO_OP = 0f;
    private static final float EXTRACT = -1f;

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

        var location = new WrapperLocation(level, pos.immutable());
        var wrapper = wrappers.computeIfAbsent(location, ComposterWrapper::new);
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

    private class Top implements ISingleResourceHandler<ItemResource> {
        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

            // Check that no action is scheduled.
            if (probability != NO_OP) return 0;
            // Check that the composter can accept items.
            if (location.getBlockState().getValue(ComposterBlock.LEVEL) >= 7) return 0;
            // Check that the item is compostable.
            float insertedIncreaseProbability = ComposterBlock.getValue(resource.toStack());
            if (insertedIncreaseProbability <= 0) return 0;

            // Schedule insertion.
            updateSnapshots(transaction);
            probability = insertedIncreaseProbability;
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
            return ComposterBlock.getValue(resource.toStack()) <= 0 ? 0 : 1;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return ComposterBlock.getValue(resource.toStack()) > 0;
        }

        @Override
        public String toString() {
            return "ComposterWrapper[" + location + "/top]";
        }
    }

    private class Bottom implements ISingleResourceHandler<ItemResource> {
        private static final ItemResource BONE_MEAL = ItemResource.of(Items.BONE_MEAL);

        private boolean hasBoneMeal() {
            // We only have bone meal if the level is 8 and no action was scheduled.
            return probability == NO_OP && location.getBlockState().getValue(ComposterBlock.LEVEL) == 8;
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

            // Check amount.
            if (amount < 1) return 0;
            // Check that the resource is bone meal.
            if (!BONE_MEAL.equals(resource)) return 0;
            // Check that there is bone meal to extract.
            if (!hasBoneMeal()) return 0;

            updateSnapshots(transaction);
            probability = EXTRACT;
            return 1;
        }

        @Override
        public ItemResource getResource(int index) {
            Objects.checkIndex(index, size());
            return BONE_MEAL;
        }

        @Override
        public int getAmount(int index) {
            Objects.checkIndex(index, size());
            return hasBoneMeal() ? 0 : 1;
        }

        @Override
        public int getCapacity(int index, ItemResource resource) {
            Objects.checkIndex(index, size());
            return BONE_MEAL.equals(resource) ? 1 : 0;
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
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
}
