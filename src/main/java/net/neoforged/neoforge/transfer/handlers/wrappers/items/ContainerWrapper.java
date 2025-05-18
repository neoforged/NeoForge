/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

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
    public int getCapacity(int index, ItemResource resource) {
        return getContainer().getMaxStackSize(resource.toStack());
    }

    @Override
    public int getCapacity(int index) {
        return getContainer().getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return getContainer().canPlaceItem(index, resource.toStack());
    }

    /**
     * Vanilla has the concept of isValid on extraction {@link Container#canTakeItem(Container, int, ItemStack)} which is not normally part of the {@link net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler IResourceHandler} contract.
     */
    public boolean isExtractable(int index, ItemResource resource) {
        return true; // canTakeItemThroughFace requires the other container that's accepting the item, so it cant be used here
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
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty() || !isValid(index, resource)) return 0;

        ResourceStack<ItemResource> stack = getContainer().getItem(index).immutable();
        if (stack.isEmpty()) {
            int insert = Math.min(amount, getCapacity(index, resource));
            if (action.isExecuting()) {
                set(index, resource, insert);
            }
            return insert;
        } else if (stack.resource().equals(resource)) {
            int insert = Math.min(amount, getCapacity(index, resource) - stack.amount());
            if (action.isExecuting()) {
                set(index, resource, stack.amount() + insert);
            }
            return insert;
        }
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty() || !isExtractable(index, resource)) return 0;
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
        return ResourceHandlerUtil.insertStacking(this, resource, amount, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return ResourceHandlerUtil.extract(this, resource, amount, action);
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContainerWrapper that)) return false;
        return container.equals(that.container);
    }

    @Override
    public int hashCode() {
        return container.hashCode() * 31;
    }
}
