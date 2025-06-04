/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import java.util.Objects;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class ResourceStorageHandler<T extends IResource> implements IResourceHandler<T> {
    /**
     * Default resource that should fill the initial buffer
     */
    protected final T defaultResource;
    /**
     * Number of indices the storage has.
     */
    protected final int size;
    /**
     * Capacity per index. This value is the same across all indices.
     */
    protected final int capacity;

    /**
     * The snapshot handler for
     */
    private final ComponentSnapshot snapshot = new ComponentSnapshot();

    public ResourceStorageHandler(int size, int capacity, T defaultResource) {
        this.size = size;
        this.capacity = capacity;
        this.defaultResource = defaultResource;
    }

    private class ComponentSnapshot extends SnapshotJournal<IResourceStorageData<T>> {
        @Override
        protected IResourceStorageData<T> createSnapshot() {
            return getContents();
        }

        @Override
        protected void revertToSnapshot(IResourceStorageData<T> snapshot) {
            setContents(snapshot);
        }

        @Override
        protected void onCommit(IResourceStorageData<T> originalState) {
            onContentsChanged();
        }
    }

    public abstract IResourceStorageData<T> getContents();

    public void setContents(IResourceStorageData<T> contents) {}

    protected void onContentsChanged() {}

    public int modifyContents(IResourceStorageData<T> contents, int requestedAmount, int changedAmount, TransactionContext context) {
        return changedAmount;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        IResourceStorageData<T> contents = getContents();
        int changedAmount = insertBehavior(contents, index, resource, amount, context);

        return modifyContents(contents, amount, changedAmount, context);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        IResourceStorageData<T> contents = getContents().attachment();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += insertBehavior(contents, i, resource, amount - changedAmount, context);
            if (changedAmount >= amount) break;
        }

        return modifyContents(contents, amount, changedAmount, context);
    }

    protected int insertBehavior(IResourceStorageData<T> contents, int index, T resource, int amount, TransactionContext transaction) {
        if (!isValid(index, resource)) return 0;

        IResourceStack<T> resourceStackInSlot = contents.get(index);
        int capacity = getCapacity(index, resource);

        if (resourceStackInSlot.isEmpty()) {
            //the slot was empty so we shall accept what capacity allows as well as set the ResourceStack
            var inserted = Math.min(capacity, amount);
            snapshot.updateSnapshots(transaction);

            contents.modify(index, resource, inserted);
            return inserted;
        }

        //There is an item in the slot already, does it match our inquiry?
        if (!resourceStackInSlot.resource().equals(resource)) return 0;

        //The resource was the same, now we grow the existing stack by how much can fit
        int inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
        snapshot.updateSnapshots(transaction);
        resourceStackInSlot.grow(inserted);
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        IResourceStorageData<T> contents = getContents().attachment();
        int changedAmount = extractBehavior(contents, index, resource, amount, context);
        return modifyContents(contents, amount, changedAmount, context);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        //OLD:
        //Get the contents and if not mutable, make it mutable ONLY if we are executing.
        // Otherwise, we can keep the existing allocations.
        //NEW: We have snapshots so we should assume they are always executing and can be reverted
        IResourceStorageData<T> contents = getContents().attachment();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount, transaction);
            if (changedAmount >= amount) break;
        }
        return modifyContents(contents, amount, changedAmount, transaction);
    }

    protected int extractBehavior(IResourceStorageData<T> contents, int index, T resource, int amount, TransactionContext transaction) {
        IResourceStack<T> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());

        snapshot.updateSnapshots(transaction);
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
        return getContents().get(index).resource();
    }

    @Override
    public int getAmount(int index) {
        return getContents().get(index).amount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        return capacity;
    }

    @Override
    public boolean isValid(int index, T resource) {
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
}
