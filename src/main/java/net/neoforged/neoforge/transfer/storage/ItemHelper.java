package net.neoforged.neoforge.transfer.storage;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class ItemHelper {
    // Just add up to 10 apples to the inventory. Return how many were inserted.

    public static int addApples(Storage<ItemResource> storage) {
        var apple = ItemResource.of(Items.APPLE);
        try (var tx = Transaction.openOuter()) {
            int inserted = storage.insert(apple, 10, tx);
            tx.commit();
            return inserted;
        }
    }

    public static int addApples(StorageWithSimulations<ItemResource> storage) {
        var apple = ItemResource.of(Items.APPLE);
        return storage.insert(apple, 10, TransferAction.EXECUTE);
    }

    // Same signature as IItemHandler#insertItem:

    public static ItemStack insertItem(Storage<ItemResource> storage, int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.openOuter()) {
            int inserted = storage.insert(slot, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    public static ItemStack insertItem(StorageWithSimulations<ItemResource> storage, int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int inserted = storage.insert(slot, ItemResource.of(stack), stack.getCount(), simulate ? TransferAction.SIMULATE : TransferAction.EXECUTE);
        int leftover = stack.getCount() - inserted;
        return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
    }

    // Extracts 16 coal from slot 0 and inserts 1 diamond into slot 1. Only if both succeed.
    // Returns true if both operations succeeded, false otherwise.

    public static boolean coalToDiamonds(Storage<ItemResource> storage, boolean simulate) {
        var coal = ItemResource.of(Items.COAL);
        var diamond = ItemResource.of(Items.DIAMOND);

        try (var tx = Transaction.openOuter()) {
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

    public static boolean coalToDiamonds(StorageWithSimulations<ItemResource> storage, boolean simulate) {
        var coal = ItemResource.of(Items.COAL);
        var diamond = ItemResource.of(Items.DIAMOND);

        if (storage.extract(0, coal, 16, TransferAction.SIMULATE) != 16) {
            return false;
        }
        if (storage.insert(1, diamond, 1, TransferAction.SIMULATE) != 1) {
            return false;
        }
        if (simulate) {
            return true;
        }

        int coalExtracted = storage.extract(0, coal, 16, TransferAction.EXECUTE);
        if (coalExtracted == 16) {
            int diamondsInserted = storage.insert(1, diamond, 1, TransferAction.EXECUTE);
            if (diamondsInserted == 1) {
                return true;
            }
        }

        // TODO: Here something failed and we need to give the coal back if possible, or buffer it...
        return false;
    }
}
