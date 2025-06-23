/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import java.util.Objects;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An {@link ISingleResourceHandler} that allows extraction of an unlimited amount of a specified resource.
 *
 * @param <T> The type of resource that this storage can accept.
 */
public class InfiniteResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    /**
     * Resource that should be provided infinitely. Mustn't be {@code null}.
     */
    public T infinite;

    public InfiniteResourceHandler(T resource) {
        this.infinite = resource;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext context) {
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0; // doesn't allow insertions
    }

    @Override
    public int extract(T resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return resource.equals(infinite) ? amount : 0;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return infinite;
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return Integer.MAX_VALUE; //This is mostly for pretty printing when displayed by mods.
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return Long.MAX_VALUE;
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        return Integer.MAX_VALUE; // Maximum capacity
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return false;
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }
}
