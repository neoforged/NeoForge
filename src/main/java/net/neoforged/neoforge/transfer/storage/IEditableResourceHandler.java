package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;

public interface IEditableResourceHandler<T extends IResource> extends IResourceHandler<T> {
    int getResourceLimit(int slot, T resource);

    void set(int slot, T resource, int amount);

    default void set(int slot, ResourceStack<T> stack) {
        set(slot, stack.resource(), stack.amount());
    }
}
