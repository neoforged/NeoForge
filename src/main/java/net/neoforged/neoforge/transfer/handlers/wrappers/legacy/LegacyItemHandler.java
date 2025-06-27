/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.legacy;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;

/**
 * A wrapper that takes an {@link IResourceHandler} bound by type {@link ItemResource}, to be used in place of {@link IItemHandler}.
 * Note, this is only provided to ease migration and shouldn't be depended on. Please try to move off of this quickly
 * should you use it.
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public final class LegacyItemHandler implements IItemHandler {
    private final IResourceHandler<ItemResource> handler;

    public LegacyItemHandler(IResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    @Override
    public int getSlots() {
        return handler.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return handler.getResource(slot).toStack(handler.getAmount(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int inserted = handler.insert(slot, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate)
                transaction.commit();
            if (stack.getCount() == inserted) return ItemStack.EMPTY;
            return stack.copyWithCount(stack.getCount() - inserted);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (TransferPreconditions.checkNonNegative(amount) == 0) return ItemStack.EMPTY;

        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            ItemResource resource = handler.getResource(slot);

            if (resource.isEmpty()) return ItemStack.EMPTY;
            int extracted = handler.extract(slot, resource, Math.min(amount, resource.getMaxStackSize()), transaction);
            if (!simulate) transaction.commit();
            return resource.toStack(extracted);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getCapacity(slot, handler.getResource(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return handler.isValid(slot, ItemResource.of(stack));
    }
}
