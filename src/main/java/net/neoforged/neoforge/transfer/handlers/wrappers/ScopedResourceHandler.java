/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A wrapper that delegates all calls to specific set of indices of a handler.
 * <p>
 * <b>By itself, this does not handle snapshotting.</b> It is expected the delegated handlers take care of what needs to be journaled.
 *
 * @param <T> The type of resource this handler manages.
 */
public class ScopedResourceHandler<T extends IResource> extends DelegatingResourceHandler<T> {
    protected int[] indices;

    public static <T extends IResource> ScopedResourceHandler<T> fromHandlerExcludingIndices(IResourceHandler<T> handler, int[] exclusions) {
        int[] indices = IntStream.range(0, handler.size())
                .filter(i -> Arrays.stream(exclusions).noneMatch(excluded -> excluded == i))
                .toArray();
        return new ScopedResourceHandler<>(handler, indices);
    }

    public ScopedResourceHandler(IResourceHandler<T> delegate, int[] indices) {
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
    public int insert(T resource, int amount, TransactionContext transaction) {
        int inserted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index : indices) {
            inserted += handler.insert(index, resource, amount - inserted, transaction);
            if (inserted >= amount)
                break;
        }
        return inserted;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        int extracted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index : indices) {
            extracted += handler.extract(index, resource, amount - extracted, transaction);
            if (extracted >= amount)
                break;
        }
        return extracted;
    }
}
