/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// TODO
public class ItemHelper {
    // Same signature as IItemHandler#insertItem:
    public static ItemStack insertItem(Storage<ItemVariant> storage, int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.open(null)) {
            int inserted = (int) storage.insert(slot, ItemVariant.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    // Same signature as IItemHandler#extractItem:

    public static ItemStack extractItem(Storage<ItemVariant> storage, int slot, int amount, boolean simulate) {
        var resource = storage.getResource(slot);
        if (resource.isBlank()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.open(null)) {
            long extracted = storage.extract(slot, resource, amount, tx);
            StoragePreconditions.notNegative(extracted);
            if (extracted > amount) {
                throw new IllegalStateException("The storage (" + storage + ") returned more (" + extracted
                        + ") from slot " + slot + " than requested (" + amount + ")");
            }
            if (extracted == 0) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                tx.commit();
            }
            return resource.toStack((int) extracted);
        }
    }
}
