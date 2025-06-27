/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An {@link IResourceHandler} that represents a handler that rejects all operations.
 * Use the {@link #instance()} method to safely cast to the resource type you are expecting. This should work for all resources.
 */
public final class EmptyResourceHandler<T extends IResource> implements IResourceHandler<T> {
    private final static EmptyResourceHandler<?> INSTANCE = new EmptyResourceHandler<>();

    public static <T extends IResource> EmptyResourceHandler<T> instance() {
        //noinspection unchecked
        return (EmptyResourceHandler<T>) INSTANCE;
    }
    // size, index-less characteristics, index-less insert, and index-less extract are all valid calls. Everything else is expected to throw due to index bounds.

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.NO_OP | TransferCharacteristics.IMMUTABLE;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        ResourceHandlerUtil.isEmpty(resource, amount);
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        ResourceHandlerUtil.isEmpty(resource, amount);
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public T getResource(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int getAmount(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int getCapacity(int index, T resource) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public boolean isValid(int index, T resource) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int characteristics(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public String toString() {
        return "EmptyResourceHandler";
    }

    private EmptyResourceHandler() {}
}
