package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// TODO
public class ItemHelper {
    // Just add up to 10 apples to the inventory. Return how many were inserted.

    public static long addApples(Storage<ItemVariant> storage) {
        var apple = ItemVariant.of(Items.APPLE);
        try (var tx = Transaction.openOuter()) {
            long inserted = storage.insert(apple, 10, tx);
            tx.commit();
            return inserted;
        }
    }

    // Same signature as IItemHandler#insertItem:

    public static ItemStack insertItem(Storage<ItemVariant> storage, int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.openOuter()) {
            int inserted = (int) storage.insert(slot, ItemVariant.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    // Extracts 16 coal from slot 0 and inserts 1 diamond into slot 1. Only if both succeed.
    // Returns true if both operations succeeded, false otherwise.

    public static boolean coalToDiamonds(Storage<ItemVariant> storage, boolean simulate) {
        var coal = ItemVariant.of(Items.COAL);
        var diamond = ItemVariant.of(Items.DIAMOND);

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
}
