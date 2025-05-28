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
    public long insert(int index, T resource, long maxAmount, TransactionContext transaction) {
        return delegate.get().insert(index, resource, maxAmount, transaction);
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
    public long extract(int index, T resource, long maxAmount, TransactionContext transaction) {
        return delegate.get().extract(index, resource, maxAmount, transaction);
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
    public boolean isResourceBlank(int index) {
        return delegate.get().isResourceBlank(index);
    }

    @Override
    public T getResource(int index) {
        return delegate.get().getResource(index);
    }

    @Override
    public long getAmount(int index) {
        return delegate.get().getAmount(index);
    }

    @Override
    public long getCapacity(int index, T resource) {
        return delegate.get().getCapacity(index, resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return delegate.get().isValid(index, resource);
    }
}
