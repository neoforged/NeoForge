/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A wrapper that delegates all calls to a single index of a handler.
 * <p>
 * <b>By itself, this does not handle snapshotting.</b> It is expected the delegated handlers take care of what needs to be journaled.
 *
 * @param <T> The type of resource this handler manages.
 */
public class SingleIndexResourceHandler<T extends IResource> extends DelegatingResourceHandler<T> {
    int index;

    public SingleIndexResourceHandler(IResourceHandler<T> delegate, int index) {
        super(delegate);
        this.index = index;
    }

    @Override
    public int size() {
        return 1;
    }

    public SingleIndexResourceHandler(Supplier<IResourceHandler<T>> delegate, int index) {
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
    public int insert(T resource, int amount, TransactionContext transaction) {
        return getDelegate().insert(index, resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        return getDelegate().extract(index, resource, amount, transaction);
    }

    public static class Modifiable<T extends IResource> extends SingleIndexResourceHandler<T> implements IResourceHandlerModifiable<T> {
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
