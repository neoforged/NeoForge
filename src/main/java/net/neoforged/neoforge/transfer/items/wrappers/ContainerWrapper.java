/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.HandlerUtils;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

public class ContainerWrapper implements IResourceHandlerModifiable<ItemResource> {
    protected final Container container;

    public ContainerWrapper(Container container) {
        this.container = container;
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        getContainer().setItem(index, resource.toStack(amount));
        getContainer().setChanged();
    }

    @Override
    public int size() {
        return getContainer().getContainerSize();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getContainer().getItem(index));
    }

    @Override
    public int getAmount(int index) {
        return getContainer().getItem(index).getCount();
    }

    @Override
    public int getLimit(int index, ItemResource resource) {
        return getContainer().getMaxStackSize(resource.toStack());
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return getContainer().canPlaceItem(index, resource.toStack());
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank() || !isValid(index, resource)) return 0;
        ResourceStack<ItemResource> stack = getContainer().getItem(index).immutable();
        if (stack.isEmpty()) {
            int insert = Math.min(amount, getLimit(index, resource));
            if (action.isExecuting()) {
                set(index, resource, insert);
            }
            return insert;
        } else if (stack.resource().equals(resource)) {
            int insert = Math.min(amount, getLimit(index, resource) - stack.amount());
            if (action.isExecuting()) {
                set(index, resource, stack.amount() + insert);
            }
            return insert;
        }
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank() || !isValid(index, resource)) return 0;
        ResourceStack<ItemResource> stack = getContainer().getItem(index).immutable();
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extract = Math.min(amount, stack.amount());
        if (action.isExecuting()) {
            set(index, resource, stack.amount() - extract);
        }
        return extract;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtils.insertStacking(this, resource, amount, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtils.extractStacking(this, resource, amount, action);
    }

    public Container getContainer() {
        return container;
    }
}
