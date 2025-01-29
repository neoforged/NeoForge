/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.SlotItemResourceWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
//Is this still necessary with our examples, and what did this do exactly? Fine with which ever, we need to fix the documentation linking
/**
 * Slot class that can be used with immutable {@link IResourceHandler IResourceHandlers}
 * like {@link ComponentItemHandler}.
 */
public class ItemHandlerCopySlot extends StackCopySlot {
    private final SlotItemResourceWrapper slotItemHandler;

    public ItemHandlerCopySlot(IResourceHandler<ItemResource> itemHandler, int index, int xPosition, int yPosition) {
        super(xPosition, yPosition);
        slotItemHandler = new SlotItemResourceWrapper(itemHandler, index, xPosition, yPosition);
    }

    public ItemHandlerCopySlot(SlotItemResourceWrapper slotItemHandler) {
        super(slotItemHandler.x, slotItemHandler.y);
        this.slotItemHandler = slotItemHandler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return slotItemHandler.mayPlace(stack);
    }

    @Override
    protected ItemStack getStackCopy() {
        return slotItemHandler.getItem();
    }

    @Override
    protected void setStackCopy(ItemStack stack) {
        ((IResourceHandlerModifiable<ItemResource>) slotItemHandler.asResourceHandler()).set(slotItemHandler.index, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
        slotItemHandler.onQuickCraft(oldStackIn, newStackIn);
    }

    @Override
    public int getMaxStackSize() {
        return slotItemHandler.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return slotItemHandler.getMaxStackSize(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return slotItemHandler.mayPickup(playerIn);
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return slotItemHandler.isSameInventory(other);
    }

    public IResourceHandler<ItemResource> getItemHandler() {
        return slotItemHandler.asResourceHandler();
    }
}
