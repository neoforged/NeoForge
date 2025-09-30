/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Helper functions to work with {@link ResourceHandler}s of {@link ItemResource}s.
 */
public final class ItemUtil {
    private ItemUtil() {}

    /**
     * Returns a new item stack with the contents of the handler at the given index.
     *
     * <p>The result's stack size may be greater than the max stack size.
     */
    public static ItemStack getStack(ResourceHandler<ItemResource> handler, int index) {
        var resource = handler.getResource(index);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return resource.toStack(handler.getAmountAsInt(index));
    }

    /**
     * Attempts to insert an item stack into a handler, leaving distribution to the handler, and returning any leftover.
     *
     * @param handler     handler to insert into
     * @param stack       the stack to insert, will not be modified by this function
     * @param simulate    {@code true} to simulate the result of the insert but leave the handler unmodified, {@code false} to modify the handler
     * @param transaction The transaction that this operation is part of.
     *                    This method will always use a nested transaction that will be rolled back.
     *                    {@code null} can be passed to conveniently have this method open its own root transaction.
     * @return the leftover: the stack of items that could <strong>not</strong> be inserted
     */
    public static ItemStack insertItemReturnRemaining(
            ResourceHandler<ItemResource> handler,
            ItemStack stack,
            boolean simulate,
            @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.open(transaction)) {
            int inserted = handler.insert(ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
        }
    }

    /**
     * Attempts to insert an item stack into the index of a handler, returning any leftover.
     *
     * @param handler     handler to insert into
     * @param index       index to insert into
     * @param stack       the stack to insert, will not be modified by this function
     * @param simulate    {@code true} to simulate the result of the insert but leave the handler unmodified, {@code false} to modify the handler
     * @param transaction The transaction that this operation is part of.
     *                    This method will always use a nested transaction that will be rolled back.
     *                    {@code null} can be passed to conveniently have this method open its own root transaction.
     * @return the leftover: the stack of items that could <strong>not</strong> be inserted
     */
    public static ItemStack insertItemReturnRemaining(
            ResourceHandler<ItemResource> handler,
            int index,
            ItemStack stack,
            boolean simulate,
            @Nullable TransactionContext transaction) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (var tx = Transaction.open(transaction)) {
            int inserted = handler.insert(index, ItemResource.of(stack), stack.getCount(), tx);
            if (!simulate) {
                tx.commit();
            }
            int leftover = stack.getCount() - inserted;
            return leftover == 0 ? ItemStack.EMPTY : stack.copyWithCount(leftover);
        }
    }
}
