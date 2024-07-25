package net.neoforged.neoforge.transfer.handlers.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.ResourceStorageContents;

public abstract class ResourceStorage<T extends IResource> implements IResourceHandler<T> {
    protected final T emptyResource;
    protected final int size;
    protected final int indexCapacity;

    public ResourceStorage(T emptyResource, int size, int indexCapacity) {
        this.emptyResource = emptyResource;
        this.size = size;
        this.indexCapacity = indexCapacity;
    }

    public abstract ResourceStorageContents<T> getContents();

    public abstract int setAndValidate(ResourceStorageContents<T> contents, int changedAmount, TransferAction action);

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = insertBehavior(contents, index, resource, amount);
        return setAndValidate(contents.build(), changedAmount, action);
    }

    protected int insertBehavior(ResourceStorageContents.Builder<T> contents, int index, T resource, int amount) {
        if (!isValid(index, resource) || !allowsInsertion(index)) return 0;
        ResourceStack<T> stack = contents.get(index);
        if (!stack.isEmpty() && !stack.resource().equals(resource)) return 0;
        int insertAmount = Math.min(amount, getCapacity(index, resource) - stack.amount());
        contents.set(index, resource, stack.amount() + insertAmount);
        return insertAmount;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = extractBehavior(contents, index, resource, amount);
        return setAndValidate(contents.build(), changedAmount, action);
    }

    protected int extractBehavior(ResourceStorageContents.Builder<T> contents, int index, T resource, int amount) {
        if (!isValid(index, resource) || !allowsExtraction(index)) return 0;
        ResourceStack<T> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());
        contents.set(index, resource, stack.amount() - extractAmount);
        return extractAmount;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += insertBehavior(contents, i, resource, amount - changedAmount);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents.build(), changedAmount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents.build(), changedAmount, action);
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
        return indexCapacity;
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
