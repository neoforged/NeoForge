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
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceFilters;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;

public class ResourceHandlerSlot extends Slot {
    private static final Container EMPTY = new SimpleContainer(0);
    private final IResourceHandler<ItemResource> handler;
    @Nullable
    private ItemStack cachedStack = null;

    public ResourceHandlerSlot(IResourceHandler<ItemResource> handler, int index, int xPosition, int yPosition) {
        super(EMPTY, index, xPosition, yPosition);
        this.handler = handler;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return handler.isValid(getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public ItemStack getItem() {
        cachedStack = handler.getResource(getSlotIndex()).toStack(handler.getAmount(getSlotIndex()));
        return cachedStack;
    }

    @Override
    public void set(ItemStack stack) {
        ((IResourceHandlerModifiable<ItemResource>) handler).set(getSlotIndex(), ItemResource.of(stack), stack.getCount());
        //this used to setChanged() are we handling that now a little more sensibly?
    }

    //From old SlotItemHandler
    @Override
    public void onQuickCraft(ItemStack oldStackIn, ItemStack newStackIn) {}

    @Override
    public int getMaxStackSize() {
        return handler.getCapacity(getSlotIndex(), ItemResource.EMPTY);
    }

    //This is now deferred to the handler rather than making the calculation here as it used to since the handler can have "getCapacity" for a specific resource
    @Override
    public int getMaxStackSize(ItemStack stack) {
        return handler.getCapacity(getSlotIndex(), ItemResource.of(stack));
    }

    @Override
    public boolean mayPickup(Player player) {
        try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            //Simulated and we do resource stack as there are less things constructed upon a new instance (micro optimization)
            return ItemUtil.extractResourceStackFilteredAtIndex(handler, ResourceFilters.any(), getSlotIndex(), 1, transaction).isEmpty();
        }
    }

    @Override
    public ItemStack remove(int amount) {
        ItemResource resource = handler.getResource(getSlotIndex());
        try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
            int extracted = handler.extract(resource, amount, transaction);
            transaction.commit();
            return extracted > 0 ? resource.toStack(extracted) : ItemStack.EMPTY;
        }
    }

    public IResourceHandler<ItemResource> asResourceHandler() {
        return handler;
    }

    @Override
    public void setChanged() {
        //Won't this cause a possible infinite loop. Not versed enough in how `vanilla` Slots/Containers work anymore so if not, then no changes needed
        if (cachedStack != null) {
            set(cachedStack);
        }
    }
}
