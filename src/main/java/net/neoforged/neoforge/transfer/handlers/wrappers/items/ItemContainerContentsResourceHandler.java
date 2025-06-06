/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Wraps the vanilla ComponentData of {@link ItemContainerContents} to allow it to be used as a {@link net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler IResourceHandler}
 */
// A very verbose name, but likely the most accurate
public class ItemContainerContentsResourceHandler implements IResourceHandlerModifiable<ItemResource> {
    protected final int size;
    protected final DataComponentType<ItemContainerContents> componentType;
    protected final IItemContext itemContext;

    public ItemContainerContentsResourceHandler(IItemContext itemContext, DataComponentType<ItemContainerContents> componentType, int size) {
        this.componentType = componentType;
        this.itemContext = itemContext;
        this.size = size;
    }

    public ItemContainerContents getContents() {
        return itemContext.getResource().getOrDefault(componentType, ItemContainerContents.fromItems(NonNullList.withSize(size(), ItemStack.EMPTY)));
    }

    public int setAndValidate(ItemContainerContents contents, int changedAmount, TransactionContext context) {
        return itemContext.exchange(itemContext.getResource().with(componentType, contents), 1, context) == 1 ? changedAmount : 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ItemResource getResource(int index) {
        // You may need to still add what you had before. Depends on if this really resulted in much better
        //return getContents().getImmutableStackInSlot(index).resource();
        return ItemResource.of(getContents().getStackInSlot(index));
    }

    @Override
    public int getAmount(int index) {
        return getContents().getStackInSlot(index).getCount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        var current = getResource(index);
        return current.isEmpty() || current.equals(resource);
    }

    @Override
    public boolean allowsInsertion(int index) {
        return true;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return true;
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        ItemContainerContents contents = getContents();
        contents.getStackInSlot(index).setCount(amount);
        try (var tx = Transaction.open(null)) {
            setAndValidate(contents, amount, tx);
            tx.commit();
        }
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !isValid(index, resource)) return 0;
        ItemContainerContents contents = getContents();
        ItemStack stack = contents.getStackInSlot(index);
        if (stack.isEmpty()) {
            amount = Math.min(amount, resource.getMaxStackSize());

            return setAndValidate(contents.with(index, resource, amount), amount, context);
        } else if (resource.matches(stack) && stack.getCount() < resource.getMaxStackSize()) {
            int newAmount = Math.min(stack.getCount() + amount, resource.getMaxStackSize());
            amount = newAmount - stack.getCount();
//            if (action.isExecuting())
            stack.grow(amount);
            return setAndValidate(contents, amount, context);
        }
        return 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        ItemContainerContents contents = getContents();
        int remaining = amount;
        for (int i = 0; i < size; i++) {
            ResourceStack<ItemResource> stack = contents.getStackInSlot(i).immutable();
            if (stack.isEmpty() || !stack.resource().equals(resource) || stack.amount() >= resource.getMaxStackSize()) continue;
            int toInsert = Math.min(remaining, resource.getMaxStackSize() - stack.amount());
            contents = contents.with(i, resource, stack.amount() + toInsert);
            remaining -= toInsert;
        }
        for (int i = 0; i < size; i++) {
            ResourceStack<ItemResource> stack = contents.getStackInSlot(i).immutable();
            if (!stack.isEmpty()) continue;
            int toInsert = Math.min(remaining, resource.getMaxStackSize());
            contents = contents.with(i, resource, toInsert);
            remaining -= toInsert;
            if (remaining <= 0) {
                break;
            }
        }
        return setAndValidate(contents, amount - remaining, context);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        ItemContainerContents contents = getContents();
        ResourceStack<ItemResource> stack = contents.getStackInSlot(index).immutable();
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extracted = Math.min(stack.amount(), amount);
        int newAmount = stack.amount() - extracted;
        contents = contents.with(index, newAmount == 0 ? ItemResource.EMPTY : stack.resource(), newAmount);
        return setAndValidate(contents, extracted, context);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext context) {
        int remaining = amount;
        ItemContainerContents contents = getContents();
        for (int slot = 0; slot < size; slot++) {
            ResourceStack<ItemResource> stack = contents.getStackInSlot(slot).immutable();
            if (stack.isEmpty() || !stack.resource().equals(resource)) continue;
            int extracted = Math.min(remaining, stack.amount());
            int newAmount = stack.amount() - extracted;
            contents = contents.with(slot, newAmount == 0 ? ItemResource.EMPTY : resource, newAmount);
            remaining -= extracted;
            if (remaining <= 0) {
                break;
            }
        }
        return setAndValidate(contents, amount - remaining, context);
    }
}
