package net.neoforged.neoforge.unittest.transfer.fluid;

import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.BucketFluidStorage;
import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.fluid.InItemStorageContext;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.item.base.ItemStackStorage;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(EphemeralTestServerProvider.class)
public class BucketFluidStorageTest {

    public static final FluidVariant WATER = FluidVariant.of(Fluids.WATER);
    public static final int BUCKET_VOLUME = FluidType.BUCKET_VOLUME;

    public BucketFluidStorageTest(MinecraftServer server) {
        // Tags need to be loaded since some functionality relies on it.
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
        Storage<FluidVariant> storage = new BucketFluidStorage(InItemStorageContext.ofStorageSlot(outerStorage, 0));

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
        void testExtractionFails() {
            try (var tx = Transaction.open(null)) {
                assertEquals(0, storage.extract(WATER, BUCKET_VOLUME, tx));
            }
        }
    }

    /**
     * Tests that even for blank underlying container items, the storage is well-behaved.
     */
    @Nested
    class EmptyHostingItem {
        Storage<FluidVariant> storage = new BucketFluidStorage(InItemStorageContext.ofStorageSlot(new ItemStackStorage(3), 0));

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
