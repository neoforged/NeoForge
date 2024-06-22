/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

public class SlotItemStorage extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);
    private final IResourceHandler<ItemResource> storage;
    private ItemStack cachedStack = null;

    public SlotItemStorage(IResourceHandler<ItemResource> storage, int index, int xPosition, int yPosition) {
        super(EMPTY, index, xPosition, yPosition);
        this.storage = storage;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return storage.isValid(getContainerSlot(), ItemResource.of(stack));
    }

    @Override
    public ItemStack getItem() {
        return cachedStack = storage.getResource(getContainerSlot()).toStack(storage.getAmount(getContainerSlot()));
    }

    @Override
    public void set(ItemStack stack) {
        ((IResourceHandlerModifiable<ItemResource>) storage).set(getContainerSlot(), ItemResource.of(stack), stack.getCount());
    }

    @Override
    public int getMaxStackSize() {
        return storage.getLimit(getContainerSlot(), ItemResource.BLANK);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return storage.getLimit(getContainerSlot(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        return storage.canExtract();
    }

    @Override
    public ItemStack remove(int amount) {
        ItemResource resource = storage.getResource(getContainerSlot());
        int extracted = storage.extract(resource, amount, TransferAction.EXECUTE);
        return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
    }

    public IResourceHandler<ItemResource> getStorage() {
        return storage;
    }

    @Override
    public void setChanged() {
        if (cachedStack != null) {
            set(cachedStack);
        }
    }
}
