package net.neoforged.neoforge.transfer.handlers.templates.storage;

import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;

import java.util.Objects;

public abstract class ResourceStorageHandler<T extends IResource> implements IResourceHandler<T> {
    /**
     * Default resource that should fill the initial buffer
     */
    protected final T emptyResource;
    /**
     * Number of indices the storage has.
     */
    protected final int size;
    /**
     * Capacity per index. This value is the same across all indices.
     */
    protected final int capacity;


    public ResourceStorageHandler(int size, int capacity, T emptyResource) {
        this.size = size;
        this.capacity = capacity;
        this.emptyResource = emptyResource;
    }

    public abstract IResourceData<T> getContents();

    public abstract int setAndValidate(IResourceData<T> contents, int requestedAmount, int changedAmount, TransferAction action);

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (amount <= 0 || resource.isEmpty()) return 0;
        IResourceData<T> contents = getContents();
        int changedAmount = insertBehavior(contents, index, resource, amount, action);
        return setAndValidate(contents, amount, changedAmount, action);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        IResourceData<T> contents = getContents().attachment();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += insertBehavior(contents, i, resource, amount - changedAmount, action);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents, amount, changedAmount, action);
    }

    protected int insertBehavior(IResourceData<T> contents, int index, T resource, int amount, TransferAction action) {
        if (!isValid(index, resource)) return 0;

        IResourceStack<T> resourceStackInSlot = contents.get(index);
        int capacity = getCapacity(index, resource);

        if (resourceStackInSlot.isEmpty()) {
            //the slot was empty so we shall accept what capacity allows as well as set the MutableResourceStack
            var inserted = Math.min(capacity, amount);
            if (action.isExecuting())
                contents.modify(index, resource, inserted);
            return inserted;
        }

        //There is an item in the slot already, does it match our inquiry?
        if (!resourceStackInSlot.resource().equals(resource)) return 0;

        //The resource was the same, now we grow the existing stack by how much can fit
        int inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
        if (action.isExecuting())
            resourceStackInSlot.grow(inserted);
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (amount <= 0 || resource.isEmpty()) return 0;
        IResourceData<T> contents = getContents().attachment();
        int changedAmount = extractBehavior(contents, index, resource, amount, action);
        return setAndValidate(contents, amount, changedAmount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        //Get the contents and if not mutable, make it mutable ONLY if we are executing.
        // Otherwise we can keep the existing allocations.
        IResourceData<T> contents = action.isExecuting() ? getContents().attachment() : getContents();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount, action);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents, amount, changedAmount, action);
    }

    protected int extractBehavior(IResourceData<T> contents, int index, T resource, int amount, TransferAction action) {
        IResourceStack<T> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());

        if (action.isExecuting()) {
            stack.shrink(extractAmount);
            contents.modify(index, !stack.isEmpty() ? stack.resource() : emptyResource, stack.amount());
        }
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
        return getCapacity(index);
    }

    @Override
    public int getCapacity(int index) {
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
