/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.templates;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.items.ItemResource;

public class ContainerContentsItemStorage implements IResourceHandlerModifiable<ItemResource> {
    protected final int size;
    protected final DataComponentType<ItemContainerContents> componentType;
    protected final IItemContext context;

    public ContainerContentsItemStorage(IItemContext context, DataComponentType<ItemContainerContents> componentType, int size) {
        this.componentType = componentType;
        this.context = context;
        this.size = size;
    }

    public ItemContainerContents getContents() {
        return context.getResource().getOrDefault(componentType, ItemContainerContents.fromItems(NonNullList.withSize(size(), ItemStack.EMPTY)));
    }

    public int setAndValidate(ItemContainerContents contents, int changedAmount, TransferAction action) {
        return context.exchange(context.getResource().set(componentType, contents), 1, action) == 1 ? changedAmount : 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ItemResource getResource(int index) {
        return getContents().getImmutableStackInSlot(index).resource();
    }

    @Override
    public int getAmount(int index) {
        return getContents().getImmutableStackInSlot(index).amount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return resource.getMaxStackSize();
    }

    @Override
    public int getCapacity(int index) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return true;
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
        ItemContainerContents contents = getContents().set(index, resource, amount);
        setAndValidate(contents, 0, TransferAction.EXECUTE);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty() || !isValid(index, resource)) return 0;
        ItemContainerContents contents = getContents();
        ResourceStack<ItemResource> stack = contents.getImmutableStackInSlot(index);
        if (stack.isEmpty()) {
            amount = Math.min(amount, resource.getMaxStackSize());
            return setAndValidate(contents.set(index, resource, amount), amount, action);
        } else if (stack.resource().equals(resource) && stack.amount() < resource.getMaxStackSize()) {
            int newAmount = Math.min(stack.amount() + amount, resource.getMaxStackSize());
            amount = newAmount - stack.amount();
            return setAndValidate(contents.set(index, resource, newAmount), amount, action);
        }
        return 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        ItemContainerContents contents = getContents();
        int remaining = amount;
        for (int i = 0; i < size; i++) {
            ResourceStack<ItemResource> stack = contents.getImmutableStackInSlot(i);
            if (stack.isEmpty() || !stack.resource().equals(resource) || stack.amount() >= resource.getMaxStackSize()) continue;
            int toInsert = Math.min(remaining, resource.getMaxStackSize() - stack.amount());
            contents = contents.set(i, resource, stack.amount() + toInsert);
            remaining -= toInsert;
        }
        for (int i = 0; i < size; i++) {
            ResourceStack<ItemResource> stack = contents.getImmutableStackInSlot(i);
            if (!stack.isEmpty()) continue;
            int toInsert = Math.min(remaining, resource.getMaxStackSize());
            contents = contents.set(i, resource, toInsert);
            remaining -= toInsert;
            if (remaining <= 0) {
                break;
            }
        }
        return setAndValidate(contents, amount - remaining, action);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        ItemContainerContents contents = getContents();
        ResourceStack<ItemResource> stack = contents.getImmutableStackInSlot(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extracted = Math.min(stack.amount(), amount);
        int newAmount = stack.amount() - extracted;
        contents = contents.set(index, newAmount == 0 ? ItemResource.NONE : stack.resource(), newAmount);
        return setAndValidate(contents, extracted, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        int remaining = amount;
        ItemContainerContents contents = getContents();
        for (int slot = 0; slot < size; slot++) {
            ResourceStack<ItemResource> stack = contents.getImmutableStackInSlot(slot);
            if (stack.isEmpty() || !stack.resource().equals(resource)) continue;
            int extracted = Math.min(remaining, stack.amount());
            int newAmount = stack.amount() - extracted;
            contents = contents.set(slot, newAmount == 0 ? ItemResource.NONE : resource, newAmount);
            remaining -= extracted;
            if (remaining <= 0) {
                break;
            }
        }
        return setAndValidate(contents, amount - remaining, action);
    }
}
