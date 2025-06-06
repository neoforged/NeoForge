/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer.fluid;

import net.minecraft.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.fluid.VanillaBucketFluidStorage;
import net.neoforged.neoforge.transfer.initem.InItemStorageContext;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.item.base.ItemStackStorage;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static net.neoforged.neoforge.unittest.transfer.fluid.StorageTestUtil.assertInventory;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(EphemeralTestServerProvider.class)
public class VanillaBucketFluidStorageTest {
    public static final FluidVariant WATER = FluidVariant.of(Fluids.WATER);
    public static final FluidVariant LAVA = FluidVariant.of(Fluids.LAVA);
    public static final int BUCKET_VOLUME = FluidType.BUCKET_VOLUME;

    public VanillaBucketFluidStorageTest() {
    }

    /**
     * Tests operating on a single empty bucket.
     */
    @Nested
    class SingleEmptyBucket {
        ItemStackStorage outerStorage = Util.make(() -> {
            var storage = new ItemStackStorage(3);
            storage.setStackInSlot(0, new ItemStack(Items.BUCKET));
            return storage;
        });
        Storage<FluidVariant> storage = new VanillaBucketFluidStorage(InItemStorageContext.ofStorageSlot(outerStorage, 0));

        @Test
        void testSizeIsOne() {
            assertEquals(1, storage.size());
        }

        @Test
        void testGetAmountIsZero() {
            assertEquals(0, storage.getAmount(0));
        }

        @Test
        void testGetResourceIsBlank() {
            assertEquals(FluidVariant.EMPTY, storage.getResource(0));
        }

        @Test
        void testInsertionOfLessThanABucketFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.insert(WATER, BUCKET_VOLUME - 1, tx));
            }
        }

        @Test
        void testInsertionAndExtractionSequence() {
            try (var tx = Transaction.open(null)) {
                assertEquals(BUCKET_VOLUME, storage.insert(WATER, BUCKET_VOLUME, tx));
                // The storage should now contain a water bucket
                assertEquals(ItemVariant.of(Items.WATER_BUCKET), outerStorage.getResource(0));

                // We can observe that there is now water in the storage, which is extractable
                assertEquals(WATER, storage.getResource(0));
                assertEquals(BUCKET_VOLUME, storage.getAmount(0));

                // Which we can extract
                assertEquals(BUCKET_VOLUME, storage.extract(0, WATER, BUCKET_VOLUME, tx));

                // Now the item is an empty bucket again
                assertEquals(ItemVariant.of(Items.BUCKET), outerStorage.getResource(0));

                // And the storage is observably empty again
                assertEquals(FluidVariant.EMPTY, storage.getResource(0));
                assertEquals(0, storage.getAmount(0));
            }
        }

        @Test
        void testFillWithWater() {
            testExpectedFilledItem(Fluids.WATER, Items.WATER_BUCKET);
        }

        @Test
        void testFillWithLava() {
            testExpectedFilledItem(Fluids.LAVA, Items.LAVA_BUCKET);
        }

        private void testExpectedFilledItem(Fluid fluid, Item expectedItem) {
            try (var tx = Transaction.open(null)) {
                assertEquals(BUCKET_VOLUME, storage.insert(FluidVariant.of(fluid), BUCKET_VOLUME, tx));
                tx.commit();
            }
            assertEquals(expectedItem, outerStorage.getStackInSlot(0).getItem());
        }

        @Test
        void testExtractionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.extract(WATER, BUCKET_VOLUME, tx));
            }
        }

        @Test
        void testGetCapacity() {
            assertEquals(BUCKET_VOLUME, storage.getCapacity(0, WATER));
            assertEquals(BUCKET_VOLUME, storage.getCapacity(0, LAVA));
            assertEquals(BUCKET_VOLUME, storage.getCapacity(0, FluidVariant.EMPTY));
        }
    }

    /**
     * Tests operating on a more than one empty bucket.
     */
    @Nested
    class MultipleEmptyBuckets {
        ItemStackStorage outerStorage = Util.make(() -> {
            var storage = new ItemStackStorage(3);
            storage.setStackInSlot(0, new ItemStack(Items.BUCKET, 3));
            return storage;
        });
        Storage<FluidVariant> storage = new VanillaBucketFluidStorage(InItemStorageContext.ofStorageSlot(outerStorage, 0));

        @Test
        void testSizeIsOne() {
            assertEquals(1, storage.size());
        }

        @Test
        void testGetAmountIsZero() {
            assertEquals(0, storage.getAmount(0));
        }

        @Test
        void testGetResourceIsBlank() {
            assertEquals(FluidVariant.EMPTY, storage.getResource(0));
        }

        @Test
        void testInsertionOfLessThanABucketFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.insert(WATER, BUCKET_VOLUME - 1, tx));
            }
        }

        @Test
        void testInsertionAndExtractionSequence() {
            try (var tx = Transaction.open(null)) {
                assertEquals(BUCKET_VOLUME, storage.insert(WATER, BUCKET_VOLUME, tx));
                // The filled water bucket cannot be stacked with the two remaining empty buckets
                // and should be moved into the second slot of the outer storage.
                assertInventory(outerStorage, new ItemStack(Items.BUCKET, 2), Items.WATER_BUCKET);

                // Since the water bucket was moved out, the focus is still on the two remaining buckets
                // which now have reduced capacity
                assertEquals(FluidVariant.EMPTY, storage.getResource(0));
                assertEquals(0, storage.getAmount(0));
                assertEquals(2 * BUCKET_VOLUME, storage.getCapacity(0, FluidVariant.EMPTY));

                // Since the bucket got moved, the water cannot be extracted
                assertEquals(0, storage.extract(0, WATER, BUCKET_VOLUME, tx));

                // We now insert a bucket of lava into the storage
                assertEquals(BUCKET_VOLUME, storage.insert(0, LAVA, BUCKET_VOLUME, tx));

                // Similarly to the water bucket, this gets moved into the outer storage
                assertInventory(outerStorage, Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);

                // Now the focus is on a single bucket
                assertEquals(FluidVariant.EMPTY, storage.getResource(0));
                assertEquals(0, storage.getAmount(0));
                assertEquals(BUCKET_VOLUME, storage.getCapacity(0, FluidVariant.EMPTY));

                // And filling that single bucket results in a filled bucket that *can* be put into the first slot
                // This means the filled fluid can then be extracted again using the same storage
                // The behavior should now be identical to a bucket as in the SingleEmptyBucket test
                assertEquals(BUCKET_VOLUME, storage.insert(0, WATER, BUCKET_VOLUME, tx));
                assertInventory(outerStorage, Items.WATER_BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);
                assertEquals(BUCKET_VOLUME, storage.extract(0, WATER, BUCKET_VOLUME, tx));
                assertInventory(outerStorage, Items.BUCKET, Items.WATER_BUCKET, Items.LAVA_BUCKET);
            }
        }

        @Test
        void testFillWithWater() {
            fill(Fluids.WATER);
            assertInventory(outerStorage,
                    new ItemStack(Items.BUCKET, 2),
                    Items.WATER_BUCKET
            );
        }

        @Test
        void testFillWithLava() {
            fill(Fluids.LAVA);
            assertInventory(outerStorage,
                    new ItemStack(Items.BUCKET, 2),
                    Items.LAVA_BUCKET
            );
        }

        private void testInventoryAfterFill(Fluid fluid, Item expectedItem) {
            fill(fluid);
            assertEquals(expectedItem, outerStorage.getStackInSlot(0).getItem());
        }

        @Test
        void testExtractionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.extract(WATER, BUCKET_VOLUME, tx));
            }
        }

        @Test
        void testGetCapacity() {
            assertEquals(3 * BUCKET_VOLUME, storage.getCapacity(0, WATER));
            assertEquals(3 * BUCKET_VOLUME, storage.getCapacity(0, LAVA));
            assertEquals(3 * BUCKET_VOLUME, storage.getCapacity(0, FluidVariant.EMPTY));
        }

        private void fill(Fluid fluid) {
            try (var tx = Transaction.open(null)) {
                assertEquals(BUCKET_VOLUME, storage.insert(FluidVariant.of(fluid), BUCKET_VOLUME, tx));
                tx.commit();
            }
        }

    }

    /**
     * Tests that even for blank underlying container items, the storage is well-behaved.
     */
    @Nested
    class EmptyHostingItem {
        Storage<FluidVariant> storage = new VanillaBucketFluidStorage(InItemStorageContext.ofStorageSlot(new ItemStackStorage(3), 0));

        @Test
        void testSizeIsZero() {
            assertEquals(0, storage.size());
        }

        @Test
        void testGetAmountIsZero() {
            assertEquals(0, storage.getAmount(0));
        }

        @Test
        void testGetResourceIsBlank() {
            assertEquals(FluidVariant.EMPTY, storage.getResource(0));
        }

        @Test
        void testInsertionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.insert(WATER, BUCKET_VOLUME, tx));
            }
        }

        @Test
        void testExtractionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.extract(WATER, BUCKET_VOLUME, tx));
            }
        }

        @Test
        void testGetCapacity() {
            assertEquals(0, storage.getCapacity(0, WATER));
        }
    }

    /**
     * These tests validate the storage behavior when the item hosting the storage is swapped out while a reference to
     * the storage is still held.
     */
    @Nested
    class SwappedOutHostingItem {
        ItemStackStorage outerStorage = Util.make(() -> {
            var storage = new ItemStackStorage(3);
            storage.setStackInSlot(0, new ItemStack(Items.BUCKET));
            return storage;
        });
        Storage<FluidVariant> storage = new VanillaBucketFluidStorage(InItemStorageContext.ofStorageSlot(outerStorage, 0));

        @BeforeEach
        void swapOutItem() {
            outerStorage.setStackInSlot(0, new ItemStack(Items.STICK));
        }

        @Test
        void testSize() {
            assertEquals(0, storage.size());
        }

        @Test
        void testInsertionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.insert(WATER, BUCKET_VOLUME, tx));
            }
        }

        @Test
        void testExtractionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.extract(WATER, BUCKET_VOLUME, tx));
            }
        }
    }
}
