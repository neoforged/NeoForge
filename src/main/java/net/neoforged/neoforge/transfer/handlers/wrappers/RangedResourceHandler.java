/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A resource handler that wraps a range of indices of another handler.
 */
public class RangedResourceHandler<T extends IResource> extends DelegatingResourceHandler<T> {
    protected int start;
    protected int end;

    public RangedResourceHandler(IResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    public RangedResourceHandler(Supplier<IResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        Preconditions.checkArgument(end > start, "Max index must be greater than min index");
        this.start = start;
        this.end = end;
    }

    public static <T extends IResource> RangedResourceHandler<T> ofSingleIndex(IResourceHandler<T> delegate, int index) {
        return new RangedResourceHandler<>(delegate, index, index + 1);
    }

    public static <T extends IResource> RangedResourceHandler<T> ofSingleIndex(Supplier<IResourceHandler<T>> delegate, int index) {
        return new RangedResourceHandler<>(delegate, index, index + 1);
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
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int extracted = 0;
        IResourceHandler<T> handler = getDelegate();
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
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int inserted = 0;
        IResourceHandler<T> handler = getDelegate();
        for (int index = start; index < end; index++) {
            inserted += handler.insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) {
                return inserted;
            }
        }

        return inserted;
    }
}
