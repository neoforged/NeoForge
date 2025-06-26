/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import java.util.Objects;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
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
    // size, insert, and extract are all valid calls. Everything else is expected to throw due to index bounds.
    // The secondary throws are never expected to be hit but are there as a safety precaution
    // instead of returning a dummy value.

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        //No-op
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        ResourceHandlerUtil.isEmpty(resource, amount);
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0");
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        ResourceHandlerUtil.isEmpty(resource, amount);
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0");
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0 and therefore have no resource");
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0 and therefore have no amount");
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0 and therefore has no capacity");
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0 and therefore is never valid");
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0 and therefore has does not support insertion");
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        throw new IndexOutOfBoundsException("Empty resource handlers are of size 0 and therefore has does not support extraction");
    }

    @Override
    public String toString() {
        return "EmptyResourceHandler";
    }

    private EmptyResourceHandler() {}
}
