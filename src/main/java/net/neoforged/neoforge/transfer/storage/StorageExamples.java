package net.neoforged.neoforge.transfer.storage;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class StorageExamples {
    // Just add up to 10 apples to the inventory. Return how many were inserted.

    public static long addApples(Storage<ItemVariant> storage) {
        var apple = ItemVariant.of(Items.APPLE);
        try (var tx = Transaction.open(null)) {
            long inserted = storage.insert(apple, 10, tx);
            tx.commit();
            return inserted;
        }
    }

    // Extracts 16 coal from slot 0 and inserts 1 diamond into slot 1. Only if both succeed.
    // Returns true if both operations succeeded, false otherwise.

    public static boolean coalToDiamonds(Storage<ItemVariant> storage, boolean simulate) {
        var coal = ItemVariant.of(Items.COAL);
        var diamond = ItemVariant.of(Items.DIAMOND);

        try (var tx = Transaction.open(null)) {
            if (storage.extract(0, coal, 16, tx) != 16) {
                return false;
            }
            if (storage.insert(1, diamond, 1, tx) != 1) {
                return false;
            }
            if (!simulate) {
                tx.commit();
            }
            return true;
        }
    }
}
