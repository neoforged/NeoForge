/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.Objects;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A wrapper that delegates all calls to a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class DelegatingResourceHandler<T extends IResource> implements IResourceHandler<T> {
    protected final Supplier<IResourceHandler<T>> delegate;

    public DelegatingResourceHandler(IResourceHandler<T> delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public DelegatingResourceHandler(Supplier<IResourceHandler<T>> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int size() {
        return getDelegate().size();
    }

    protected int convertIndex(int index) {
        Objects.checkIndex(index, size());
        return index;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return getDelegate().getResource(convertIndex(index));
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getDelegate().getAmount(convertIndex(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return getDelegate().getAmountAsLong(convertIndex(index));
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        return getDelegate().getCapacity(convertIndex(index), resource);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        return getDelegate().getCapacityAsLong(convertIndex(index), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return getDelegate().isValid(convertIndex(index), resource);
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return getDelegate().supportsInsertion(convertIndex(index));
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return getDelegate().supportsExtraction(convertIndex(index));
    }

    @Override
    public boolean supportsInsertion() {
        return getDelegate().supportsInsertion();
    }

    @Override
    public boolean supportsExtraction() {
        return getDelegate().supportsExtraction();
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        return getDelegate().insert(convertIndex(index), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        return getDelegate().insert(resource, amount, transaction);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        return getDelegate().extract(convertIndex(index), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        return getDelegate().extract(resource, amount, transaction);
    }

    public IResourceHandler<T> getDelegate() {
        return delegate.get();
    }
}
