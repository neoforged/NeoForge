/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

public class ItemTests {
    @Test
    void testStackReference() {
        // Ensure that Container wrappers will try to mutate the backing stack as much as possible.
        // In many cases, MC code captures a reference to the ItemStack so we want to edit that stack directly
        // and not a copy whenever we can. Obviously this can't be perfect, but we try to cover as many cases as possible.
        SimpleContainer container = new SimpleContainer(new ItemStack(Items.DIAMOND, 2));
        var containerWrapper = VanillaContainerWrapper.of(container);
        ItemStack stack = container.getItem(0);

        // Simulate should correctly reset the stack.
        try (Transaction tx = Transaction.openRoot()) {
            containerWrapper.extract(ItemResource.of(Items.DIAMOND), 2, tx);
        }

        if (stack != container.getItem(0)) throw new AssertionError("Stack should have stayed the same.");

        // Commit should modify the count of the original stack.
        try (Transaction tx = Transaction.openRoot()) {
            containerWrapper.extract(ItemResource.of(Items.DIAMOND), 1, tx);
            tx.commit();
        }

        if (stack != container.getItem(0)) throw new AssertionError("Stack should have stayed the same.");

        // Also edit the stack when the item matches, even when the components and the count change.
        ItemResource oldResource = ItemResource.of(Items.DIAMOND);
        ItemResource newResource = oldResource.with(DataComponents.MAX_DAMAGE, 10);

        try (Transaction tx = Transaction.openRoot()) {
            containerWrapper.extract(oldResource, 1, tx);
            containerWrapper.insert(newResource, 5, tx);
            tx.commit();
        }

        if (stack != container.getItem(0)) throw new AssertionError("Stack should have stayed the same.");
        if (!stackEquals(stack, newResource, 5)) throw new AssertionError("Failed to update stack components or count.");
    }

    @Test
    void testContainerWrappers() {
        ItemResource emptyBucket = ItemResource.of(Items.BUCKET);
        TestWorldlyContainer testContainer = new TestWorldlyContainer();
        checkComparatorOutput(testContainer);

        // Create a few wrappers.
        var unsidedWrapper = VanillaContainerWrapper.of(testContainer);
        var downWrapper = new WorldlyContainerWrapper(testContainer, Direction.DOWN);
        var upWrapper = new WorldlyContainerWrapper(testContainer, Direction.UP);

        // Make sure querying a new wrapper returns the same one.
        if (VanillaContainerWrapper.of(testContainer) != unsidedWrapper) throw new AssertionError("Wrappers should be ==");

        for (int iter = 0; iter < 2; ++iter) {
            // First time, abort.
            // Second time, commit.
            try (Transaction transaction = Transaction.openRoot()) {
                // Insert bucket from down - should fail.
                if (downWrapper.insert(emptyBucket, 1, transaction) != 0) throw new AssertionError("Bucket should not have been inserted.");
                // Insert bucket unsided - should go in slot 1 (canPlaceItem returns false for slot 0).
                if (unsidedWrapper.insert(emptyBucket, 1, transaction) != 1) throw new AssertionError("Failed to insert bucket.");
                if (!testContainer.getItem(0).isEmpty()) throw new AssertionError("Slot 0 should have been empty.");
                if (!stackEquals(testContainer.getItem(1), Items.BUCKET, 1)) throw new AssertionError("Slot 1 should have been a bucket.");
                // The bucket should be extractable from any side but the top.
                if (!emptyBucket.equals(ResourceHandlerUtil.findExtractableResource(unsidedWrapper, r -> true, transaction))) throw new AssertionError("Bucket should be extractable from unsided wrapper.");
                if (!emptyBucket.equals(ResourceHandlerUtil.findExtractableResource(downWrapper, r -> true, transaction))) throw new AssertionError("Bucket should be extractable from down wrapper.");
                if (ResourceHandlerUtil.findExtractableResource(upWrapper, r -> true, transaction) != null) throw new AssertionError("Bucket should NOT be extractable from up wrapper.");

                if (iter == 1) {
                    // Commit the second time only.
                    transaction.commit();
                }
            }
        }

        // Check commit.
        if (!testContainer.getItem(0).isEmpty()) throw new AssertionError("Slot 0 should have been empty.");
        if (!testContainer.getItem(1).is(Items.BUCKET) || testContainer.getItem(1).getCount() != 1) throw new AssertionError("Slot 1 should have been a single bucket.");

        checkComparatorOutput(testContainer);

        // Check that we return sensible results if amount stored > capacity
        ItemStack oversizedStack = new ItemStack(Items.DIAMOND_PICKAXE, 2);
        SimpleContainer simpleContainer = new SimpleContainer(oversizedStack);
        var wrapper = VanillaContainerWrapper.of(simpleContainer);

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(0L, wrapper.insert(ItemResource.of(oversizedStack), 10, transaction));
            transaction.commit();
        }
    }

    private static boolean stackEquals(ItemStack stack, Item item, int count) {
        return stackEquals(stack, ItemResource.of(item), count);
    }

    private static boolean stackEquals(ItemStack stack, ItemResource variant, int count) {
        return variant.matches(stack) && stack.getCount() == count;
    }

    private static class TestWorldlyContainer extends SimpleContainer implements WorldlyContainer {
        private static final int[] SLOTS = { 0, 1, 2 };

        TestWorldlyContainer() {
            super(SLOTS.length);
        }

        @Override
        public int[] getSlotsForFace(Direction face) {
            return SLOTS;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot != 0 || !stack.is(Items.BUCKET); // can't have buckets in slot 0.
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
            return dir != Direction.DOWN;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
            return dir != Direction.UP;
        }
    }

    /**
     * Test insertion when {@link Container#getMaxStackSize()} is the bottleneck.
     */
    @Test
    void testLimitedStackCountContainer() {
        ItemResource diamond = ItemResource.of(Items.DIAMOND);
        SimpleContainer container = new SimpleContainer(diamond.toStack(), diamond.toStack(), diamond.toStack()) {
            @Override
            public int getMaxStackSize() {
                return 3;
            }
        };
        var wrapper = VanillaContainerWrapper.of(container);

        // Should only be able to insert 2 diamonds per stack * 3 stacks = 6 diamonds.
        try (Transaction transaction = Transaction.openRoot()) {
            if (wrapper.insert(diamond, 1000, transaction) != 6) {
                throw new AssertionError("Only 6 diamonds should have been inserted.");
            }

            checkComparatorOutput(container);
        }
    }

    /**
     * Test insertion when {@link Item#getMaxStackSize} is the bottleneck.
     */
    @Test
    void testLimitedStackCountItem() {
        ItemResource diamondPickaxe = ItemResource.of(Items.DIAMOND_PICKAXE);
        SimpleContainer container = new SimpleContainer(5);
        var wrapper = VanillaContainerWrapper.of(container);

        // Should only be able to insert 5 pickaxes, as the item limits stack counts to 1.
        try (Transaction transaction = Transaction.openRoot()) {
            if (wrapper.insert(diamondPickaxe, 1000, transaction) != 5) {
                throw new AssertionError("Only 5 pickaxes should have been inserted.");
            }

            checkComparatorOutput(container);
        }
    }

    private static void checkComparatorOutput(Container container) {
        var wrapper = VanillaContainerWrapper.of(container);

        int vanillaOutput = AbstractContainerMenu.getRedstoneSignalFromContainer(container);
        int transferApiOutput = ResourceHandlerUtil.getRedstoneSignalFromResourceHandler(wrapper);

        if (vanillaOutput != transferApiOutput) {
            String error = String.format(
                    "Vanilla and Transfer API comparator outputs should have been identical. Vanilla: %d. Transfer API: %d.",
                    vanillaOutput,
                    transferApiOutput);
            throw new AssertionError(error);
        }
    }

    /**
     * Ensure that SimpleContainer only calls setChanged at the end of a successful transaction.
     */
    @Test
    void testSimpleContainerUpdates() {
        var simpleContainer = new SimpleContainer(2) {
            boolean throwOnSetChanges = true;
            boolean setChangesCalled = false;

            @Override
            public void setChanged() {
                if (throwOnSetChanges) {
                    throw new AssertionError("Unexpected setChanged call!");
                }

                setChangesCalled = true;
            }
        };
        var wrapper = VanillaContainerWrapper.of(simpleContainer);
        ItemResource diamond = ItemResource.of(Items.DIAMOND);

        // Simulation should not trigger notifications.
        try (Transaction tx = Transaction.openRoot()) {
            wrapper.insert(diamond, 1000, tx);
        }

        // But commit after modification should.
        try (Transaction tx = Transaction.openRoot()) {
            wrapper.insert(diamond, 1000, tx);

            simpleContainer.throwOnSetChanges = false;
            tx.commit();
        }

        if (!simpleContainer.setChangesCalled) {
            throw new AssertionError("setChanged should have been called when committing.");
        }
    }
}
