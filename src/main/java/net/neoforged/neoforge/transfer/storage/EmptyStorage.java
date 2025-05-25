package net.neoforged.neoforge.transfer.storage;

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
    public final long insert(int index, T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public final long extract(int index, T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public final boolean isResourceBlank(int index) {
        return true;
    }

    @Override
    public final T getResource(int index) {
        return getBlankResource();
    }

    @Override
    public final long getAmount(int index) {
        return 0;
    }

    @Override
    public final long getCapacity(int index, T resource) {
        return 0;
    }

    @Override
    public final boolean isValid(int index, T resource) {
        return false;
    }

    /**
     * @return The blank resource to return when an empty slot is queried.
     */
    protected abstract T getBlankResource();
}
