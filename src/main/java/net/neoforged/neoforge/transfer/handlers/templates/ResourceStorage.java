package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.*;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.ResourceStorageContents;

import java.util.Objects;

public abstract class ResourceStorage<T extends IResource> implements IResourceHandler<T> {
    //Should we make this a ResourceStack which would encode a default amount as well?
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


    public ResourceStorage(int size, int capacity, T emptyResource) {
        this.size = size;
        this.capacity = capacity;
        this.emptyResource = emptyResource;
    }

    public abstract ResourceStorageContents<T> getContents();

    public abstract int setAndValidate(ResourceStorageContents<T> contents, int changedAmount, TransferAction action);

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (amount <= 0 || resource.isEmpty()) return 0;
        ResourceStorageContents<T> contents = getContents();
        int changedAmount = insertBehavior(contents, index, resource, amount, action);
        return setAndValidate(contents, changedAmount, action);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        ResourceStorageContents<T> contents = getContents();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += insertBehavior(contents, i, resource, amount - changedAmount, action);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents, changedAmount, action);
    }

    protected int insertBehavior(ResourceStorageContents<T> contents, int index, T resource, int amount, TransferAction action) {
        if (!isValid(index, resource)) return 0;

        MutableResourceStack<T> resourceStackInSlot = contents.get(index);
        int capacity = getCapacity(index, resource);

        if (resourceStackInSlot.isEmpty()) {
            //the slot was empty so we shall accept what capacity allows as well as set the MutableResourceStack
            var inserted = Math.min(capacity, amount);
            if(action.isExecuting())
                contents.set(index, MutableResourceStack.of(resource, inserted));
            return inserted;
        }

        //There is an item in the slot already, does it match our inquiry?
        if (!resourceStackInSlot.resource().equals(resource)) return 0;

        //The resource was the same, now we grow the existing stack by how much can fit
        int inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
        if(action.isExecuting())
            resourceStackInSlot.grow(inserted);
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        Objects.checkIndex(index, size()); // We want to short circuit if someone tries to insert in a different index. This will throw
        if (amount <= 0 || resource.isEmpty()) return 0;
        ResourceStorageContents<T> contents = getContents();
        int changedAmount = extractBehavior(contents, index, resource, amount, action);
        return setAndValidate(contents, changedAmount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        ResourceStorageContents<T> contents = getContents();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount, action);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents, changedAmount, action);
    }

    protected int extractBehavior(ResourceStorageContents<T> contents, int index, T resource, int amount, TransferAction action) {
        // Do we actually care about the resource validity for extraction?
        //     This COULD be expensive if a mod chooses to use isValid for recipe validations.
        // Also allowsExtraction (much like insert) is a hint for external users of this handler.
        //    It is intended for something like pipes to precache an assumption of what they are able to do with a given handler,
        //    not a logic check to use every operation.

//        if (!isValid(index, resource) /*|| !allowsExtraction(index)*/) return 0;
        IResourceStack<T> stack = contents.get(index);  // changed this to be a MutableResourceStack to remove the allocation per operation.
        //However, from this change, I don't know how this affects the setAndValidate with something like a bottle of honey or similar. So we may need to look into that when compiler errors are gone
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());

        // We likely want to set to the empty resource if we went below 0
        if (action.isExecuting() && stack.shrink(extractAmount).isEmpty())
            contents.set(index, MutableResourceStack.of(emptyResource, 0));
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
    public boolean isValid(T resource) {
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
