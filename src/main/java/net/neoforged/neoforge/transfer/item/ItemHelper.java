/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.primitives.Ints;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Helper functions to work with {@link Storage}s of {@link ItemVariant}s.
 */
// TODO
public class ItemHelper {
    private ItemHelper() {}

    /**
     * Returns the ItemStack in a given slot.
     *
     * <p>The result's stack size may be greater than the itemstack's max size.
     *
     * <p>If the result is empty, then the slot is empty.
     *
     * @param slot Slot to query
     * @return ItemStack in given slot. Empty Itemstack if the slot is empty.
     **/
    public static ItemStack getStackInSlot(Storage<ItemVariant> storage, int slot) {
        ItemVariant resource = storage.getResource(slot);
        if (resource.isBlank()) {
            return ItemStack.EMPTY;
        }
        return resource.toStack(Ints.saturatedCast(storage.getAmount(slot)));
    }

    /**
     * Inserts an ItemStack into the given slot and returns <strong>the remainder</strong>.
     * The ItemStack will not be modified in this function!
     *
     * <p>This function is a drop-in replacement for the old {@code IItemHandler#insertItem}.
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     */
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

    // Same signature as IItemHandler#extractItem:

    public static ItemStack extractItem(Storage<ItemVariant> storage, int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        var resource = storage.getResource(slot);
        if (resource.isBlank()) {
            return ItemStack.EMPTY;
        }
        amount = Math.min(amount, resource.getMaxStackSize());
        try (var tx = Transaction.openOuter()) {
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
