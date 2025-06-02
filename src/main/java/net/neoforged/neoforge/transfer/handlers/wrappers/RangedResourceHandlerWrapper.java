/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import com.google.common.base.Preconditions;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * A wrapper that delegates all calls to a range of indices of a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class RangedResourceHandlerWrapper<T extends IResource> extends DelegatingResourceHandlerWrapper<T> {
    protected int start;
    protected int end;

    public RangedResourceHandlerWrapper(IResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);

    }

    public RangedResourceHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        Preconditions.checkArgument(end > start, "Max slot must be greater than min slot");
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

    public static class Modifiable<T extends IResource> extends RangedResourceHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
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
