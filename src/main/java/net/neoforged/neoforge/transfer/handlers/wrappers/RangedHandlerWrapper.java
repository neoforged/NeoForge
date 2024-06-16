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
 * A wrapper that delegates all calls to a range of indices of a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class RangedHandlerWrapper<T extends IResource> extends DelegatingHandlerWrapper<T> {
    protected int start;
    protected int end;

    public RangedHandlerWrapper(IResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    public RangedHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    protected int convertIndex(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return index + start;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        int extracted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount) {
                return extracted;
            }
        }

        return extracted;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        int inserted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        return inserted;
    }

    public static class Modifiable<T extends IResource> extends RangedHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
        public Modifiable(IResourceHandlerModifiable<T> delegate, int start, int end) {
            super(delegate, start, end);
        }

        public Modifiable(Supplier<IResourceHandlerModifiable<T>> delegate, int start, int end) {
            super(delegate::get, start, end);
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
