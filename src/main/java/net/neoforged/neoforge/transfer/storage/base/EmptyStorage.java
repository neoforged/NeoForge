package net.neoforged.neoforge.transfer.storage.base;

import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An immutable, empty storage.
 */
public abstract class EmptyStorage<T> implements Storage<T> {
    @Override
    public final int size() {
        return 0;
    }

    @Override
    public final long insert(int slot, T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public final long extract(int slot, T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public final boolean isResourceBlank(int slot) {
        return true;
    }

    @Override
    public final T getResource(int slot) {
        return getBlankResource();
    }

    @Override
    public final long getAmount(int slot) {
        return 0;
    }

    @Override
    public final long getCapacity(int slot, T resource) {
        return 0;
    }

    @Override
    public final boolean isValid(int slot, T resource) {
        return false;
    }

    /**
     * @return The blank resource to return when an empty slot is queried.
     */
    protected abstract T getBlankResource();
}
