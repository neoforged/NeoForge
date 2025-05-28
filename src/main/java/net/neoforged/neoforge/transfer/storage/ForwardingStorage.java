package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@link Storage} that delegates each method to another {@link Storage}.
 * The {@code Supplier} is re-evaluated each time a method is called.
 */
public class ForwardingStorage<T> implements Storage<T> {
    protected final Supplier<? extends Storage<T>> delegate;

    public ForwardingStorage(Storage<T> delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public ForwardingStorage(Supplier<? extends Storage<T>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return delegate.get().size();
    }

    @Override
    public long insert(int slot, T resource, long maxAmount, TransactionContext transaction) {
        return delegate.get().insert(slot, resource, maxAmount, transaction);
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        return delegate.get().insert(resource, maxAmount, transaction);
    }

    @Override
    public boolean supportsInsertion() {
        return delegate.get().supportsInsertion();
    }

    @Override
    public long extract(int slot, T resource, long maxAmount, TransactionContext transaction) {
        return delegate.get().extract(slot, resource, maxAmount, transaction);
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        return delegate.get().extract(resource, maxAmount, transaction);
    }

    @Override
    public boolean supportsExtraction() {
        return delegate.get().supportsExtraction();
    }

    @Override
    public boolean isResourceBlank(int slot) {
        return delegate.get().isResourceBlank(slot);
    }

    @Override
    public T getResource(int slot) {
        return delegate.get().getResource(slot);
    }

    @Override
    public long getAmount(int slot) {
        return delegate.get().getAmount(slot);
    }

    @Override
    public long getCapacity(int slot, T resource) {
        return delegate.get().getCapacity(slot, resource);
    }

    @Override
    public boolean isValid(int slot, T resource) {
        return delegate.get().isValid(slot, resource);
    }
}
