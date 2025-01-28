/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class SlotItemResourceWrapper extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);
    private final IResourceHandler<ItemResource> handler;
    private ItemStack cachedStack = null;

    public SlotItemResourceWrapper(IResourceHandler<ItemResource> handler, int index, int xPosition, int yPosition) {
        super(EMPTY, index, xPosition, yPosition);
        this.handler = handler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return handler.isValid(getContainerSlot(), ItemResource.of(stack));
    }

    @Override
    public ItemStack getItem() {
        return cachedStack = handler.getResource(getContainerSlot()).toStack(handler.getAmount(getContainerSlot()));
    }

    @Override
    public void set(ItemStack stack) {
        ((IResourceHandlerModifiable<ItemResource>) handler).set(getContainerSlot(), ItemResource.of(stack), stack.getCount());
    }

    @Override
    public int getMaxStackSize() {
        return handler.getCapacity(getContainerSlot());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacity(getContainerSlot(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        return handler.allowsExtraction();
    }

    @Override
    public ItemStack remove(int amount) {
        ItemResource resource = handler.getResource(getContainerSlot());
        int extracted = handler.extract(resource, amount, TransferAction.EXECUTE);
        return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
    }

    public IResourceHandler<ItemResource> asResourceHandler() {
        return handler;
    }

    @Override
    public void setChanged() {
        if (cachedStack != null) {
            set(cachedStack);
        }
    }
}
