/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.google.common.math.IntMath;
import java.util.Objects;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class ResourceStorageComponentHandler<T extends IResource> implements IResourceHandler<T> {
    /**
     * Default resource that should fill the initial buffer
     */
    protected final T defaultResource;
    protected final DataComponentType<ResourceStorageComponent<T>> componentType;
    protected final int size;
    protected final IItemContext itemContext;
    /**
     * Capacity per index. This value is the same across all indices.
     */
    protected final int capacity;

    private final IStackFactory<T, ResourceStack<T>> stackFactory;

    protected ResourceStorageComponentHandler(IItemContext context, DataComponentType<ResourceStorageComponent<T>> componentType, int size, int capacity, T defaultResource, IStackFactory<T, ResourceStack<T>> stackFactory) {
        this.itemContext = context;
        this.componentType = componentType;
        this.size = size;
        this.capacity = capacity;
        this.defaultResource = defaultResource;
        this.stackFactory = stackFactory;
    }

    protected ResourceStorageComponent<T> getContents() {
        var resource = itemContext.getResource();
        var component = resource.get(componentType);
        if (component != null) return component;
        return ResourceStorageComponent.of(size, defaultResource, stackFactory);
    }

    public int modifyContents(ResourceStorageComponent<T> contents, int requestedAmount, int changedAmount, TransactionContext transaction) {
        if (changedAmount == 0) return 0;
        int exchangeCount = requestedAmount / changedAmount;
        //context in this case is the stack storing the items
        ItemResource context = itemContext.getResource().with(componentType, contents.immutable());
        int result = itemContext.exchange(context, exchangeCount, transaction);
        return IntMath.saturatedMultiply(result, changedAmount);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        ResourceStorageComponent.Mutable<T> contents = getContents().mutable();
        int changedAmount = insertBehavior(contents, index, resource, amount, context);

        return modifyContents(contents.immutable(), amount, changedAmount, context);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        ResourceStorageComponent.Mutable<T> contents = getContents().mutable();
        int changedAmount = 0;
        int cachedSize = size();
        for (int i = 0; i < cachedSize; i++) {
            changedAmount += insertBehavior(contents, i, resource, amount - changedAmount, context);
            if (changedAmount >= amount) break;
        }

        return modifyContents(contents.immutable(), amount, changedAmount, context);
    }

    protected int insertBehavior(ResourceStorageComponent.Mutable<T> contents, int index, T resource, int amount, TransactionContext transaction) {
        if (!isValid(index, resource)) return 0;

        IResourceStack<T> resourceStackInSlot = contents.get(index);
        int capacity = getCapacity(index, resource);

        if (resourceStackInSlot.isEmpty()) {
            //the slot was empty so we shall accept what capacity allows as well as set the ResourceStack
            int inserted = Math.min(capacity, amount);
            //            snapshot.get(index).updateSnapshots(transaction);
            contents.modify(index, resource, inserted);
            return inserted;
        }

        //There is an item in the slot already, does it match our inquiry?
        if (!resourceStackInSlot.resource().equals(resource)) return 0;

        //The resource was the same, now we grow the existing stack by how much can fit
        int inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
        //        snapshot.get(index).updateSnapshots(transaction);
        resourceStackInSlot.grow(inserted);
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        ResourceStorageComponent.Mutable<T> contents = getContents().mutable();
        int changedAmount = extractBehavior(contents, index, resource, amount, context);
        return modifyContents(contents.immutable(), amount, changedAmount, context);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        //grab a mutable copy of our resources to work with so that if we happen to extract from multiple indices we aren't needing to create a new list every time
        ResourceStorageComponent.Mutable<T> contents = getContents().mutable();
        int changedAmount = 0;
        int cachedSize = size();
        for (int i = 0; i < cachedSize; i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount, transaction);
            if (changedAmount >= amount) break;
        }
        //Make it immutable again for the item component
        return modifyContents(contents.immutable(), amount, changedAmount, transaction);
    }

    protected int extractBehavior(ResourceStorageComponent.Mutable<T> contents, int index, T resource, int amount, TransactionContext transaction) {
        IResourceStack<T> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());

        //        snapshot.get(index).updateSnapshots(transaction);
        stack.shrink(extractAmount);
        contents.modify(index, !stack.isEmpty() ? stack.resource() : defaultResource, stack.amount());
        return extractAmount;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return getContents().get(index).resource();
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getContents().get(index).amount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        return capacity;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return true;
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return true;
    }
}
