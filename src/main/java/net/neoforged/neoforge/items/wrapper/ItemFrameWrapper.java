/*
 * Copyright (c) Forge Development LLC and contributors SPDX-License-Identifier: LGPL-2.1-only
 */
package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class ItemFrameWrapper implements IItemHandlerModifiable {
    protected final ItemFrame itemFrame;

    public ItemFrameWrapper(ItemFrame itemFrame) {
        this.itemFrame = itemFrame;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return itemFrame.getItem();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (itemFrame.getItem().isEmpty() && !simulate) {
            itemFrame.setItem(stack);
            return stack.getCount() == 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
        }
        // itemFrame has stack of size 1, so nothing to insert
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;

        validateSlotIndex(slot);
        ItemStack stackInSlot = itemFrame.getItem();

        if (stackInSlot.isEmpty()) return ItemStack.EMPTY;

        if (simulate) {
            return stackInSlot.copy();
        } else {
            itemFrame.setItem(ItemStack.EMPTY);
            return stackInSlot;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        itemFrame.setItem(stack);
    }

    private void validateSlotIndex(int slot) {
        if (slot != 0) throw new IllegalArgumentException("Only slot 0 is present - requested: " + slot);
    }
}
