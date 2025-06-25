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
import net.neoforged.neoforge.transfer.ResourceFilters;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IIndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;

public class ResourceHandlerSlot extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);
    private final IResourceHandler<ItemResource> handler;
    private final IIndexModifier<ItemResource> slotModifier;

    public ResourceHandlerSlot(IResourceHandler<ItemResource> handler, int index, int xPosition, int yPosition, IIndexModifier<ItemResource> slotModifier) {
        super(EMPTY, index, xPosition, yPosition);
        this.handler = handler;
        this.slotModifier = slotModifier;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return handler.isValid(getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public boolean hasItem() {
        return !handler.getResource(getSlotIndex()).isEmpty();
    }

    @Override
    public ItemStack getItem() {
        return handler.getResource(getSlotIndex()).toStack(handler.getAmount(getSlotIndex()));
    }

    @Override
    public void set(ItemStack stack) {
        slotModifier.set(getSlotIndex(), ItemResource.of(stack), stack.getCount());
        setChanged();
    }

    //From old SlotItemHandler
    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return handler.getCapacity(getSlotIndex(), ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacity(getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        return ResourceHandlerUtil.hasExtractableResourceAtIndex(handler, ResourceFilters.any(), getSlotIndex());
    }

    @Override
    public ItemStack remove(int amount) {
        var slotIndex = getSlotIndex();
        ItemResource resource = handler.getResource(slotIndex);
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int extracted = handler.extract(slotIndex, resource, amount, transaction);

            if (extracted > 0) return ItemStack.EMPTY;

            transaction.commit();
            return ItemStack.EMPTY;
        }
    }

    public IResourceHandler<ItemResource> asResourceHandler() {
        return handler;
    }

    public IIndexModifier<ItemResource> getSlotModifier() {
        return slotModifier;
    }
}
