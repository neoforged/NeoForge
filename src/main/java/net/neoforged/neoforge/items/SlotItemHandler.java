/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotItemHandler extends Slot {
    private static Container emptyInventory = new SimpleContainer(0);
    private final IItemHandler.Slot slot;

    public SlotItemHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.slot = itemHandler.getSlot(index);
    }

    public SlotItemHandler(IItemHandler.Slot slot, int index, int xPosition, int yPosition) {
        super(emptyInventory, index, xPosition, yPosition);
        this.slot = slot;
    }

    public SlotItemHandler(IItemHandler.Slot slot, int xPosition, int yPosition) {
        super(emptyInventory, slot.index(), xPosition, yPosition);
        this.slot = slot;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return slot.test(stack);
    }

    @Override
    public ItemStack getItem() {
        return slot.get();
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    @Override
    public void set(ItemStack stack) {
        slot.set(stack);
        this.setChanged();
    }

    // Override if your IItemHandler does not implement IItemHandlerModifiable
    // @Override
    public void initialize(ItemStack stack) {
        slot.set(stack);
        this.setChanged();
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return slot.limit();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        int maxSize = stack.getMaxStackSize();
        ItemStack maxStack = stack.copyWithCount(maxSize);

        ItemStack currentStack = this.slot.get();
        ItemStack remainder = this.slot.insert(maxStack, true);
        int current = currentStack.getCount();
        int added = maxSize - remainder.getCount();
        return current + added;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !this.slot.extract(1, true).isEmpty();
    }

    @Override
    public ItemStack remove(int amount) {
        return this.slot.extract(amount, false);
    }

    public IItemHandler getItemHandler() {
        return slot.handler();
    }
/* TODO Slot patches
@Override
public boolean isSameInventory(Slot other)
{
return other instanceof SlotItemHandler && ((SlotItemHandler) other).getItemHandler() == this.itemHandler;
}*/
}
