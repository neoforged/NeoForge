/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.Objects;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * A wrapper that delegates all calls to a handler.
 * <p>
 * <b>By itself, this does not handle snapshotting.</b> It is expected the delegated handlers take care of what needs to be journaled.
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
        return index;
    }

    @Override
    public T getResource(int index) {
        return getDelegate().getResource(convertIndex(index));
    }

    @Override
    public int getAmount(int index) {
        return getDelegate().getAmount(convertIndex(index));
    }

    @Override
    public int getCapacity(int index, T resource) {
        return getDelegate().getCapacity(convertIndex(index), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return getDelegate().isValid(convertIndex(index), resource);
    }

    @Override
    public boolean allowsInsertion(int index) {
        return getDelegate().allowsInsertion(convertIndex(index));
    }

    @Override
    public boolean allowsExtraction(int index) {
        return getDelegate().allowsExtraction(convertIndex(index));
    }

    @Override
    public boolean allowsInsertion() {
        return getDelegate().allowsInsertion();
    }

    @Override
    public boolean allowsExtraction() {
        return getDelegate().allowsExtraction();
    }

    @Override
    public int insert(int index, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, TransactionContext transaction) {
        return getDelegate().insert(convertIndex(index), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, TransactionContext transaction) {
        return getDelegate().insert(resource, amount, transaction);
    }

    @Override
    public int extract(int index, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, TransactionContext transaction) {
        return getDelegate().extract(convertIndex(index), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, TransactionContext transaction) {
        return getDelegate().extract(resource, amount, transaction);
    }

    public IResourceHandler<T> getDelegate() {
        return delegate.get();
    }

    public static class Modifiable<T extends IResource> extends DelegatingResourceHandler<T> implements IResourceHandlerModifiable<T> {
        public Modifiable(IResourceHandlerModifiable<T> delegate) {
            super(delegate);
        }

        public Modifiable(Supplier<IResourceHandlerModifiable<T>> delegate) {
            super(delegate::get);
        }

        @Override
        public void set(int index, T resource, int amount) {
            getDelegate().set(convertIndex(index), resource, amount);
        }

        @Override
        public IResourceHandlerModifiable<T> getDelegate() {
            return (IResourceHandlerModifiable<T>) super.getDelegate();
        }
    }
}
