/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.Objects;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A resource handler that wraps a range of indices of another handler.
 */
public class RangedResourceHandler<T extends Resource> extends DelegatingResourceHandler<T> {
    /**
     * Creates a wrapper for a range of indices.
     *
     * @param start start of the range of indices, inclusive
     * @param end   end of the range of indices, exclusive
     */
    public static <T extends Resource> RangedResourceHandler<T> of(ResourceHandler<T> delegate, int start, int end) {
        return new RangedResourceHandler<>(delegate, start, end);
    }

    /**
     * Creates a wrapper for a range of indices, with the passed supplier being queried every time the handler is accessed.
     *
     * @param start start of the range of indices, inclusive
     * @param end   end of the range of indices, exclusive
     */
    public static <T extends Resource> RangedResourceHandler<T> of(Supplier<ResourceHandler<T>> delegate, int start, int end) {
        return new RangedResourceHandler<>(delegate, start, end);
    }

    /**
     * Creates a wrapper for a single index.
     */
    public static <T extends Resource> RangedResourceHandler<T> ofSingleIndex(ResourceHandler<T> delegate, int index) {
        return new RangedResourceHandler<>(delegate, index, index + 1);
    }

    /**
     * Creates a wrapper for a single index, with the passed supplier being queried every time the handler is accessed.
     */
    public static <T extends Resource> RangedResourceHandler<T> ofSingleIndex(Supplier<ResourceHandler<T>> delegate, int index) {
        return new RangedResourceHandler<>(delegate, index, index + 1);
    }

    protected int start;
    protected int end;

    protected RangedResourceHandler(ResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    protected RangedResourceHandler(Supplier<ResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        if (start < 0 || start >= end) {
            throw new IndexOutOfBoundsException("Invalid range: start=" + start + ", end=" + end);
        }
        int delegateSize = delegate.get().size();
        if (end > delegateSize) {
            throw new IndexOutOfBoundsException("Invalid range: end " + end + " is larger than the size of the handler " + delegateSize);
        }
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    protected int convertIndex(int index) {
        Objects.checkIndex(index, size());
        return index + start;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int extracted = 0;
        ResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            extracted += handler.extract(index, resource, amount - extracted, transaction);
            if (extracted == amount) {
                return extracted;
            }
        }

        return extracted;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;
        ResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            inserted += handler.insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) {
                return inserted;
            }
        }

        return inserted;
    }
}
