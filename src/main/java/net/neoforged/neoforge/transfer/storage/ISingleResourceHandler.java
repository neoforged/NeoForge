package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

public interface ISingleResourceHandler<T extends IResource> extends IResourceHandler<T> {
    @Override
    default int getSlotCount() {
        return 1;
    }

    T getResource();

    @Override
    default T getResource(int slot) {
        return getResource();
    }

    int getAmount();

    @Override
    default int getAmount(int slot) {
        return getAmount();
    }

    int getLimit();

    @Override
    default int getSlotLimit(int slot) {
        return getLimit();
    }

    boolean isResourceValid(T resource);

    @Override
    default boolean isResourceValid(int slot, T resource) {
        return isResourceValid(resource);
    }

    @Override
    default int insert(int slot, T resource, int amount, TransferAction action) {
        return insert(resource, amount, action);
    }

    @Override
    default int extract(int slot, T resource, int amount, TransferAction action) {
        return extract(resource, amount, action);
    }
}
