/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.resources.IndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class ResourceHandlerSlot extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);
    private final ResourceHandler<ItemResource> handler;
    private final IndexModifier<ItemResource> slotModifier;

    public ResourceHandlerSlot(ResourceHandler<ItemResource> handler, int index, int xPosition, int yPosition, IndexModifier<ItemResource> slotModifier) {
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
        return handler.getResource(getSlotIndex()).toStack(handler.getAmountAsInt(getSlotIndex()));
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
        return handler.getCapacityAsInt(getSlotIndex(), ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacityAsInt(getSlotIndex(), ItemResource.of(stack));
    }

    // TODO: fix
//    @Override
//    public boolean mayPickup(Player player) {
//        return ResourceHandlerUtil.hasExtractableResourceAtIndex(handler, resource -> true, getSlotIndex());
//    }
//
//    @Override
//    public ItemStack remove(int amount) {
//        var slotIndex = getSlotIndex();
//        ItemResource resource = handler.getResource(slotIndex);
//        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
//            int extracted = handler.extract(slotIndex, resource, amount, transaction);
//
//            if (extracted > 0) return ItemStack.EMPTY;
//
//            transaction.commit();
//            return resource.toStack(extracted);
//        }
//    }
//
//    public IResourceHandler<ItemResource> asResourceHandler() {
//        return handler;
//    }
//
//    public IIndexModifier<ItemResource> getSlotModifier() {
//        return slotModifier;
//    }
}
