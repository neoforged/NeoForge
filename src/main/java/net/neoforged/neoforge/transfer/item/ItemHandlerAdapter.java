/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.storage.Storage;

/**
 * Adapts a {@link Storage} to {@link IItemHandler} with auto-commit behavior.
 */
public final class ItemHandlerAdapter implements IItemHandler {
    private final Storage<ItemVariant> storage;

    public ItemHandlerAdapter(Storage<ItemVariant> storage) {
        this.storage = storage;
    }

    @Override
    public int getSlots() {
        return storage.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return storage.getResource(slot).toStack();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return ItemHelper.insertItem(storage, slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemHelper.extractItem(storage, slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return clampToMaxAmount(storage.getCapacity(slot, ItemVariant.EMPTY));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return storage.isValid(slot, ItemVariant.of(stack));
    }

    private static int clampToMaxAmount(long amount) {
        return Math.clamp(amount, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
