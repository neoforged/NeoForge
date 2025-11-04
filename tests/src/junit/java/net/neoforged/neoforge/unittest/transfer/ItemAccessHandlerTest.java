/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.HandlerItemAccess;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.junit.jupiter.api.Test;

class ItemAccessHandlerTest {
    @Test
    void testFluidItemApi() {
        FluidResource water = FluidResource.of(Fluids.WATER);
        ItemResource waterBucket = ItemResource.of(Items.WATER_BUCKET);
        Container testContainer = new BucketTestContainer(ItemStack.EMPTY, new ItemStack(Items.BUCKET), new ItemStack(Items.WATER_BUCKET));

        ResourceHandler<FluidResource> slot1Handler = new BucketResourceHandler(new StackingItemAccess(testContainer, 1).oneByOne());
        ResourceHandler<FluidResource> slot2Handler = new BucketResourceHandler(new StackingItemAccess(testContainer, 2).oneByOne());

        try (Transaction transaction = Transaction.openRoot()) {
            // Test extract.
            if (slot2Handler.extract(water, FluidType.BUCKET_VOLUME, transaction) != FluidType.BUCKET_VOLUME) throw new AssertionError("Should have extracted from full bucket.");
            // Test that an empty bucket was added.
            if (!sameItemSameCount(testContainer.getItem(1), Items.BUCKET, 2)) throw new AssertionError("Buckets should have stacked.");
            // Test that we can't extract again
            if (slot2Handler.extract(water, FluidType.BUCKET_VOLUME, transaction) != 0) throw new AssertionError("Should not have extracted a second time.");
            // Now insert water into slot 1.
            if (slot1Handler.insert(water, FluidType.BUCKET_VOLUME, transaction) != FluidType.BUCKET_VOLUME) throw new AssertionError("Failed to insert.");
            // Check that it filled slot 0.
            if (!sameItemSameCount(testContainer.getItem(0), Items.WATER_BUCKET, 1)) throw new AssertionError("Should have filled slot 0.");
            // Now we yeet the bucket from slot 0 just because we can.
            if (VanillaContainerWrapper.of(testContainer).extract(0, waterBucket, 1, transaction) != 1) throw new AssertionError("Failed to yeet bucket from slot 0.");
            // Now insert should fill slot 1 with a bucket.
            if (slot1Handler.insert(water, FluidType.BUCKET_VOLUME, transaction) != FluidType.BUCKET_VOLUME) throw new AssertionError("Failed to insert.");
            // Check container contents.
            if (!testContainer.getItem(0).isEmpty()) throw new AssertionError("Slot 0 should have been empty.");
            if (!sameItemSameCount(testContainer.getItem(1), Items.WATER_BUCKET, 1)) throw new AssertionError("Should have filled slot 1 with a water bucket.");
        }

        // Check contents after abort
        if (!testContainer.getItem(0).isEmpty()) throw new AssertionError("Failed to abort slot 0.");
        if (!sameItemSameCount(testContainer.getItem(1), Items.BUCKET, 1)) throw new AssertionError("Failed to abort slot 1.");
        if (!sameItemSameCount(testContainer.getItem(2), Items.WATER_BUCKET, 1)) throw new AssertionError("Failed to abort slot 2.");
    }

    private static boolean sameItemSameCount(ItemStack stack, Item item, int count) {
        return stack.is(item) && stack.getCount() == count;
    }

    private static class BucketTestContainer extends SimpleContainer {
        BucketTestContainer(ItemStack... stacks) {
            super(stacks);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return slot != 2; // Forbid insertion into slot 2.
        }
    }

    private static class StackingItemAccess extends HandlerItemAccess {
        public StackingItemAccess(Container container, int index) {
            super(VanillaContainerWrapper.of(container), index);
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

            int inserted = handler.insert(index, resource, amount, transaction);
            if (inserted < amount) {
                inserted += ResourceHandlerUtil.insertStacking(handler, resource, amount - inserted, transaction);
            }
            return inserted;
        }
    }
}
