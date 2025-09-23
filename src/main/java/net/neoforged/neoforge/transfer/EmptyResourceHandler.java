/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An empty resource handler.
 * <p>It has zero indices and rejects all operations.
 * <p>Use {@link #instance()} to obtain an empty handler for any resource type.
 */
public final class EmptyResourceHandler<T extends Resource> implements ResourceHandler<T> {
    private static final EmptyResourceHandler<?> INSTANCE = new EmptyResourceHandler<>();

    /**
     * Returns an empty resource handler for the desired resource type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Resource> EmptyResourceHandler<T> instance() {
        return (EmptyResourceHandler<T>) INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public T getResource(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public long getAmountAsLong(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public boolean isValid(int index, T resource) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public String toString() {
        return "EmptyResourceHandler";
    }

    private EmptyResourceHandler() {}
}
