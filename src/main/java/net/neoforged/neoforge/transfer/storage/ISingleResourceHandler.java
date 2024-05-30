package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

public interface ISingleResourceHandler<T extends IResource> extends IResourceHandler<T> {
    @Override
    default int size() {
        return 1;
    }

    T getResource();

    @Override
    default T getResource(int index) {
        return getResource();
    }

    int getAmount();

    @Override
    default int getAmount(int index) {
        return getAmount();
    }

    int getLimit(T resource);

    @Override
    default int getLimit(int index, T resource) {
        return getLimit(resource);
    }

    boolean isValid(T resource);

    @Override
    default boolean isValid(int index, T resource) {
        return isValid(resource);
    }

    @Override
    default int insert(int index, T resource, int amount, TransferAction action) {
        return insert(resource, amount, action);
    }

    @Override
    default int extract(int index, T resource, int amount, TransferAction action) {
        return extract(resource, amount, action);
    }
}
