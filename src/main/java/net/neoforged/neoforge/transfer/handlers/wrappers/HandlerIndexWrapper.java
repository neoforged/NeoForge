/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

import java.util.function.Supplier;

/**
 * A wrapper that delegates all calls to a single index of a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class HandlerIndexWrapper<T extends IResource> extends DelegatingHandlerWrapper<T> {
    int index;

    public HandlerIndexWrapper(IResourceHandler<T> delegate, int index) {
        super(delegate);
        this.index = index;
    }

    public HandlerIndexWrapper(Supplier<IResourceHandler<T>> delegate, int index) {
        super(delegate);
        this.index = index;
    }

    @Override
    protected int convertIndex(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return this.index;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return getDelegate().insert(index, resource, amount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return getDelegate().extract(index, resource, amount, action);
    }

    public static class Modifiable<T extends IResource> extends HandlerIndexWrapper<T> implements IResourceHandlerModifiable<T> {
        public Modifiable(IResourceHandlerModifiable<T> delegate, int index) {
            super(delegate, index);
        }

        public Modifiable(Supplier<IResourceHandlerModifiable<T>> delegate, int index) {
            super(delegate::get, index);
        }

        @Override
        public IResourceHandlerModifiable<T> getDelegate() {
            return (IResourceHandlerModifiable<T>) super.getDelegate();
        }

        @Override
        public void set(int index, T resource, int amount) {
            getDelegate().set(this.index, resource, amount);
        }

        public void set(T resource, int amount) {
            getDelegate().set(this.index, resource, amount);
        }
    }
}
