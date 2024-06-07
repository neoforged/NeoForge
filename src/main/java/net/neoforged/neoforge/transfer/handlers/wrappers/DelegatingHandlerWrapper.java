/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

import java.util.Objects;
import java.util.function.Supplier;

public class DelegatingHandlerWrapper<T extends IResource> implements IResourceHandler<T> {
    protected final Supplier<IResourceHandler<T>> delegate;

    public DelegatingHandlerWrapper(IResourceHandler<T> delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public DelegatingHandlerWrapper(Supplier<IResourceHandler<T>> delegate) {
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
    public int getLimit(int index, T resource) {
        return getDelegate().getLimit(convertIndex(index), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return getDelegate().isValid(convertIndex(index), resource);
    }

    @Override
    public boolean canInsert() {
        return getDelegate().canInsert();
    }

    @Override
    public boolean canExtract() {
        return getDelegate().canExtract();
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

    public static class Modifiable<T extends IResource> extends DelegatingHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
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
