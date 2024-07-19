/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import com.google.common.base.Preconditions;
import net.minecraft.world.item.ItemStack;

public interface IItemHandlerModifiable extends IItemHandler {
    /**
     * Overrides the stack in the given slot. This method is used by the
     * standard Forge helper methods and classes. It is not intended for
     * general use by other mods, and the handler may throw an error if it
     * is called unexpectedly.
     *
     * @param slot  Slot to modify
     * @param stack ItemStack to set slot to (may be empty).
     * @throws RuntimeException if the handler is called in a way that the handler
     *                          was not expecting.
     **/
    void setStackInSlot(int slot, ItemStack stack);

    @Override
    default Slot getSlot(int index) {
        Preconditions.checkElementIndex(index, this.getSlots());
        return new Slot() {
            @Override
            public boolean test(ItemStack stack) {
                return IItemHandlerModifiable.this.isItemValid(index, stack);
            }

            @Override
            public ItemStack get() {
                return IItemHandlerModifiable.this.getStackInSlot(index);
            }

            @Override
            public void set(ItemStack stack) {
                IItemHandlerModifiable.this.setStackInSlot(index, stack);
            }

            @Override
            public ItemStack insert(ItemStack stack, boolean simulate) {
                return IItemHandlerModifiable.this.insertItem(index, stack, simulate);
            }

            @Override
            public ItemStack extract(int amount, boolean simulate) {
                return IItemHandlerModifiable.this.extractItem(index, amount, simulate);
            }

            @Override
            public int limit() {
                return IItemHandlerModifiable.this.getSlotLimit(index);
            }

            @Override
            public int index() {
                return index;
            }

            @Override
            public IItemHandler handler() {
                return IItemHandlerModifiable.this;
            }

            @Override
            public boolean equals(Object other) {
                if (this == other)
                    return true;
                if (!(other instanceof Slot slot))
                    return false;
                return IItemHandlerModifiable.this == slot.handler() && index == slot.index();
            }

            @Override
            public int hashCode() {
                return IItemHandlerModifiable.this.hashCode() + index * 31;
            }
        };
    }
}
