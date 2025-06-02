/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.Objects;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * A wrapper that delegates all calls to a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class DelegatingResourceHandlerWrapper<T extends IResource> implements IResourceHandler<T> {
    protected final Supplier<IResourceHandler<T>> delegate;

    public DelegatingResourceHandlerWrapper(IResourceHandler<T> delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public DelegatingResourceHandlerWrapper(Supplier<IResourceHandler<T>> delegate) {
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
    public int getCapacity(int index) {
        return getDelegate().getCapacity(convertIndex(index));
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
    public int insert(int index, T resource, int amount, TransferAction action) {
        return getDelegate().insert(convertIndex(index), resource, amount, action);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return getDelegate().insert(resource, amount, action);
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        return getDelegate().extract(convertIndex(index), resource, amount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return getDelegate().extract(resource, amount, action);
    }

    public IResourceHandler<T> getDelegate() {
        return delegate.get();
    }

    public static class Modifiable<T extends IResource> extends DelegatingResourceHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
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
