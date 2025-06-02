/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * A wrapper that delegates all calls to specific set of indices of a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class ScopedResourceHandlerWrapper<T extends IResource> extends DelegatingResourceHandlerWrapper<T> {
    protected int[] indices;

    public static <T extends IResource> ScopedResourceHandlerWrapper<T> fromHandlerExcludingIndices(IResourceHandler<T> handler, int[] exclusions) {
        int[] indices = IntStream.range(0, handler.size())
                .filter(i -> Arrays.stream(exclusions).noneMatch(excluded -> excluded == i))
                .toArray();
        return new ScopedResourceHandlerWrapper<>(handler, indices);
    }

    public ScopedResourceHandlerWrapper(IResourceHandler<T> delegate, int[] indices) {
        super(delegate);
        this.indices = indices;
    }

    public ScopedResourceHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int[] indices) {
        super(delegate);
        this.indices = indices;
    }

    @Override
    public int size() {
        return indices.length;
    }

    @Override
    protected int convertIndex(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return indices[index];
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        int inserted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index : indices) {
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount)
                break;
        }
        return inserted;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        int extracted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index : indices) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount)
                break;
        }
        return extracted;
    }

    public static class Modifiable<T extends IResource> extends ScopedResourceHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
        public Modifiable(IResourceHandlerModifiable<T> delegate, int[] indices) {
            super(delegate, indices);
        }

        public Modifiable(Supplier<IResourceHandlerModifiable<T>> delegate, int[] indices) {
            super(delegate::get, indices);
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
