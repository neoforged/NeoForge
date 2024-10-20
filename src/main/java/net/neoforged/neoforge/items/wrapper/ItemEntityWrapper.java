/*
 * Copyright (c) Forge Development LLC and contributors SPDX-License-Identifier: LGPL-2.1-only
 */
package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class ItemEntityWrapper implements IItemHandlerModifiable {
    protected final ItemEntity itemEntity;

    public ItemEntityWrapper(ItemEntity itemEntity) {
        this.itemEntity = itemEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return itemEntity.getItem();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        ItemStack stackInSlot = itemEntity.getItem();
        if (!ItemEntity.areMergable(stackInSlot, stack)) {
            return stack;
        }

        int insertableCount = Math.min(stackInSlot.getMaxStackSize() - stackInSlot.getCount(), stack.getCount());
        if (!simulate) {
            stackInSlot.grow(insertableCount);
            itemEntity.setItem(stackInSlot);
        }
        return stack.getCount() == insertableCount ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - insertableCount);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) return ItemStack.EMPTY;

        validateSlotIndex(slot);
        ItemStack stackInSlot = itemEntity.getItem();

        if (stackInSlot.isEmpty()) return ItemStack.EMPTY;

        if (stackInSlot.getCount() < amount) {
            if (!simulate) {
                itemEntity.setItem(ItemStack.EMPTY);
                itemEntity.discard();
            }
            return stackInSlot.copy();
        } else {
            if (simulate)

                return stackInSlot.copyWithCount(amount);

            ItemStack extracted = stackInSlot.split(amount);
            itemEntity.setItem(stackInSlot);
            return extracted;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return itemEntity.getItem().getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        itemEntity.setItem(stack);
    }

    private void validateSlotIndex(int slot) {
        if (slot != 0) throw new IllegalArgumentException("Only slot 0 is present - requested: " + slot);
    }
}
