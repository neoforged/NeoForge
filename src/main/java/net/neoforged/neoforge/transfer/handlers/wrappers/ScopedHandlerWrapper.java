/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * A wrapper that delegates all calls to specific set of indices of a handler.
 *
 * @param <T> The type of resource this handler manages.
 */
public class ScopedHandlerWrapper<T extends IResource> extends DelegatingHandlerWrapper<T> {
    protected int[] indices;

    public static <T extends IResource> ScopedHandlerWrapper<T> fromHandlerExcludingIndices(IResourceHandler<T> handler, int[] exclusions) {
        int[] indices = new int[handler.size() - exclusions.length];
        List<Integer> exclusionList = Arrays.stream(exclusions).boxed().toList();
        int index = 0;
        for (int i = 0; i < handler.size(); i++) {
            if (!exclusionList.contains(i)) {
                indices[index++] = i;
            }
        }
        return new ScopedHandlerWrapper<>(handler, indices);
    }

    public ScopedHandlerWrapper(IResourceHandler<T> delegate, int[] indices) {
        super(delegate);
        this.indices = indices;
    }

    public ScopedHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int[] indices) {
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
            if (inserted >= amount) {
                return inserted;
            }
        }
        return inserted;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        int extracted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index : indices) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }

    public static class Modifiable<T extends IResource> extends ScopedHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
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
