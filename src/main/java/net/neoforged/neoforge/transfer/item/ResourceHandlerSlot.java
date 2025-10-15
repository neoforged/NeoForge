/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IndexModifier;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.StacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.world.inventory.StackCopySlot;

/**
 * Base implementation for a {@link Slot} backed by a {@code ResourceHandler<ItemResource>}.
 * Requires the handler to expose a {@linkplain IndexModifier direct mutation function},
 * such as {@link StacksResourceHandler#set}.
 */
public class ResourceHandlerSlot extends StackCopySlot {
    private final ResourceHandler<ItemResource> handler;
    private final IndexModifier<ItemResource> slotModifier;
    private final int index;

    public ResourceHandlerSlot(ResourceHandler<ItemResource> handler, IndexModifier<ItemResource> slotModifier, int index, int xPosition, int yPosition) {
        super(xPosition, yPosition);
        this.handler = handler;
        this.slotModifier = slotModifier;
        this.index = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        // Use isValid as a reasonable estimate.
        // We can't try to insert as we don't want to check the current contents to allow swapping.
        // This method is left for mods to override if this is not sufficient.
        return handler.isValid(index, ItemResource.of(stack));
    }

    @Override
    protected ItemStack getStackCopy() {
        return handler.getResource(index).toStack(handler.getAmountAsInt(index));
    }

    @Override
    protected void setStackCopy(ItemStack stack) {
        slotModifier.set(index, ItemResource.of(stack), stack.getCount());
    }

    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return handler.getCapacityAsInt(index, ItemResource.EMPTY);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacityAsInt(index, ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        var resource = handler.getResource(index);
        if (resource.isEmpty()) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            // Simulated extraction
            return handler.extract(index, resource, 1, tx) == 1;
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
