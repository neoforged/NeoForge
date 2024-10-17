/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * To be used in Menus.
 */
public class SlotExtension extends Slot {
    private static final Container emptyInventory = new SimpleContainer(0);
    private final IExtensionSlot slot;

    public SlotExtension(IExtensionSlot slot, int x, int y) {
        super(emptyInventory, 0, x, y);
        this.slot = slot;
    }

    /**
     * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
     */
    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;

        return slot.canEquip(stack);
    }

    /**
     * Helper fnct to get the stack in the slot.
     */
    @Override
    public ItemStack getItem() {
        return slot.getContents();
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable

    /**
     * Helper method to put a stack in the slot.
     */
    @Override
    public void set(ItemStack stack) {
        slot.setContents(stack);
        this.setChanged();
    }

    /**
     * if par2 has more items than par1, onCrafting(item,countIncrease) is called
     */
    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {

    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    @Override
    public boolean mayPickup(Player playerIn) {
        return slot.canUnequip(slot.getContents());
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack remove(int amount) {
        ItemStack itemstack = slot.getContents();

        int available = Math.min(itemstack.getCount(), amount);
        int remaining = itemstack.getCount() - available;

        ItemStack split = itemstack.copy();
        split.setCount(available);
        itemstack.setCount(remaining);

        if (remaining <= 0)
            slot.setContents(ItemStack.EMPTY);

        this.setChanged();

        return split;
    }

    public IExtensionSlot getExtensionSlot() {
        return slot;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof SlotExtension && ((SlotExtension) other).getExtensionSlot() == this.slot;
    }
}
