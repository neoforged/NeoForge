/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An empty {@link IResourceHandler}.
 * It has zero indices and rejects all operations.
 * Use the {@link #instance()} method to obtain an instance for the resource type you are expecting.
 */
public final class EmptyResourceHandler<T extends IResource> implements IResourceHandler<T> {
    private final static EmptyResourceHandler<?> INSTANCE = new EmptyResourceHandler<>();

    /**
     * Returns an instance of an empty resource handler for the given resource type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends IResource> EmptyResourceHandler<T> instance() {
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
    public long getAmount(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public long getCapacity(int index, T resource) {
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
