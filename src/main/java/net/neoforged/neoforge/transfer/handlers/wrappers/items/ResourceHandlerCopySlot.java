/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.items.ItemContextItemHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

/**
 * Slot class that can be used with immutable {@link IResourceHandler IResourceHandlers}
 * like {@link ItemContextItemHandler MCItemContentsHandler}.
 */
public class ResourceHandlerCopySlot extends StackCopySlot {
    private final ResourceHandlerSlot slotWrapper;

    public ResourceHandlerCopySlot(IResourceHandler<ItemResource> handler, int index, int xPosition, int yPosition) {
        super(xPosition, yPosition);
        slotWrapper = new ResourceHandlerSlot(handler, index, xPosition, yPosition);
    }

    public ResourceHandlerCopySlot(ResourceHandlerSlot slotWrapper) {
        super(slotWrapper.x, slotWrapper.y);
        this.slotWrapper = slotWrapper;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return slotWrapper.mayPlace(stack);
    }

    @Override
    protected ItemStack getStackCopy() {
        return slotWrapper.getItem();
    }

    @Override
    protected void setStackCopy(ItemStack stack) {
        ((IResourceHandlerModifiable<ItemResource>) slotWrapper.asResourceHandler()).set(slotWrapper.index, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {
        slotWrapper.onQuickCraft(oldStackIn, newStackIn);
    }

    @Override
    public int getMaxStackSize() {
        return slotWrapper.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return slotWrapper.getMaxStackSize(stack);
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return slotWrapper.mayPickup(playerIn);
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return slotWrapper.isSameInventory(other);
    }

    public IResourceHandler<ItemResource> asResourceHandler() {
        return slotWrapper.asResourceHandler();
    }
}
