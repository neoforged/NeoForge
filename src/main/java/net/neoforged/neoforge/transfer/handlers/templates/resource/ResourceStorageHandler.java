/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.NotificationSnapshot;

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
    private final List<ComponentSnapshot> snapshot;

    private final List<MutableResourceStack<T>> resourceSnapshots;
    private final BooleanList indexMutations;
    private final GroupSnapshot groupSnapshot = new GroupSnapshot();

    public ResourceStorageHandler(int size, int capacity, T defaultResource) {
        this.size = size;
        this.capacity = capacity;
        this.defaultResource = defaultResource;
        snapshot = new ArrayList<>(size);
        resourceSnapshots = new ArrayList<>(size);
        indexMutations = new BooleanArrayList(size);
        for (var i = 0; i < size; i++) {
            snapshot.add(new ComponentSnapshot(i));
            resourceSnapshots.add(MutableResourceStack.of(defaultResource, 0));
            indexMutations.add(false);
        }
    }

    private class GroupSnapshot extends NotificationSnapshot {
        @Override
        protected void revertToSnapshot(EmptyValue snapshot) {
            var data = getContents().attachment();

            var dataSize = data.size();
            for (var index = 0; index < dataSize; index++) {
                if (!indexMutations.getBoolean(index)) continue;

                var workingStack = resourceSnapshots.get(index);
                data.modify(index, workingStack.resource(), workingStack.amount());
                indexMutations.set(index, false);
                resourceSnapshots.set(index, MutableResourceStack.of(defaultResource, 0));
            }
            setContents(data);
        }

        @Override
        protected void onCommit(EmptyValue originalState) {
            onContentsChanged();
        }
    }

    private class ComponentSnapshot extends SnapshotJournal<IResourceStack<T>> {
        private final int index;
        //mutable work area

        public ComponentSnapshot(int index) {
            this.index = index;
        }

        @Override
        protected IResourceStack<T> createSnapshot() {
            return getContents().get(index).copy();
        }

        @Override
        public void updateSnapshots(TransactionContext transaction) {
            groupSnapshot.updateSnapshots(transaction);
            super.updateSnapshots(transaction);
        }

        @Override
        protected void revertToSnapshot(IResourceStack<T> snapshot) {
            resourceSnapshots.set(index, snapshot.mutable());
            indexMutations.set(index, true);
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
        int cachedSize = size();
        for (int i = 0; i < cachedSize; i++) {
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
            int inserted = Math.min(capacity, amount);
            snapshot.get(index).updateSnapshots(transaction);
            contents.modify(index, resource, inserted);
            return inserted;
        }

        //There is an item in the slot already, does it match our inquiry?
        if (!resourceStackInSlot.resource().equals(resource)) return 0;

        //The resource was the same, now we grow the existing stack by how much can fit
        int inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
        snapshot.get(index).updateSnapshots(transaction);
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
        int cachedSize = size();
        for (int i = 0; i < cachedSize; i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount, transaction);
            if (changedAmount >= amount) break;
        }
        return modifyContents(contents, amount, changedAmount, transaction);
    }

    protected int extractBehavior(IResourceStorageData<T> contents, int index, T resource, int amount, TransactionContext transaction) {
        IResourceStack<T> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());

        snapshot.get(index).updateSnapshots(transaction);
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
    public boolean supportsInsertion(int index) {
        return true;
    }

    @Override
    public boolean supportsExtraction(int index) {
        return true;
    }
}
