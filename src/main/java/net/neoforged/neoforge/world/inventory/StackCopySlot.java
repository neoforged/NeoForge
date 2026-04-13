/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

/**
 * Slot to handle immutable itemstack storages (Ex: {@link ItemAccessItemHandler}).
 * <p>
 * For an implementation for use with a {@link ResourceHandler} see {@link ResourceHandlerSlot}.
 * <p>
 * Vanilla MC code modifies the stack returned by {@link #getItem()} directly, but it
 * calls {@link #setChanged()} when that happens, so we just cache the returned stack,
 * and set it when {@link #setChanged()} is called.
 */
public abstract class StackCopySlot extends Slot {
    private static final Container emptyInventory = new SimpleContainer(0);

    @Nullable
    private ItemStack cachedReturnedStack = null;

    /**
     * @param slot The slot in the underlying container, whatever it may be; zero if not applicable.
     */
    public StackCopySlot(int slot, int x, int y) {
        super(emptyInventory, slot, x, y);
    }

    /**
     * Gets the itemstack from the storage.
     *
     * @return the stack in this slot
     */
    protected abstract ItemStack getStackCopy();

    /**
     * Sets the itemstack from the storage.
     *
     * @param stack the stack to put into this slot
     */
    protected abstract void setStackCopy(ItemStack stack);

    @Override
    public final ItemStack getItem() {
        return cachedReturnedStack = getStackCopy();
    }

    @Override
    public final void set(ItemStack stack) {
        setStackCopy(stack);
        cachedReturnedStack = stack;
    }

    @Override
    public final void setChanged() {
        // Verify that the stack has actually changed before setting it.
        // Vanilla menu logic (like AbstractContainerMenu#moveItemStackTo) often already sets the stack through Slot#setByPlayer.
        // This is done to prevent slot change logic from running multiple times when not necessary.
        if (cachedReturnedStack != null && !ItemStack.matches(cachedReturnedStack, getStackCopy())) {
            set(cachedReturnedStack);
        }
    }

    @Override
    public final ItemStack remove(int amount) {
        ItemStack stack = getStackCopy().copy();
        ItemStack ret = stack.split(amount);
        set(stack);
        cachedReturnedStack = null;
        return ret;
    }
}
