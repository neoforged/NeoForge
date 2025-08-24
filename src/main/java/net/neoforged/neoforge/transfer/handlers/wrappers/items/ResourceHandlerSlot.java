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
import net.neoforged.neoforge.transfer.handlers.resources.IndexModifier;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// TODO: missing javadoc
// TODO: should maybe extend StackCopySlot
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
    public ItemStack getItem() {
        return handler.getResource(getSlotIndex()).toStack(handler.getAmountAsInt(getSlotIndex()));
    }

    @Override
    public void set(ItemStack stack) {
        slotModifier.set(getSlotIndex(), ItemResource.of(stack), stack.getCount());
        setChanged();
    }

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

    @Override
    public boolean mayPickup(Player player) {
        try (var tx = Transaction.open(null)) {
            // Simulated extraction
            return handler.extract(getSlotIndex(), handler.getResource(getSlotIndex()), 1, tx) == 1;
        }
    }

    @Override
    public ItemStack remove(int amount) {
        ItemResource resource = handler.getResource(getSlotIndex());

        try (Transaction transaction = Transaction.open(null)) {
            int extracted = handler.extract(getSlotIndex(), resource, amount, transaction);
            transaction.commit();
            return resource.toStack(extracted);
        }
    }

    public ResourceHandler<ItemResource> getResourceHandler() {
        return handler;
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return other instanceof ResourceHandlerSlot rhs && rhs.handler == this.handler;
    }
}
