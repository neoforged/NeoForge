package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.IResource;

public interface IResourceHandlerModifiable<T extends IResource> extends IResourceHandler<T> {
    void set(int index, T resource, int amount);
}
