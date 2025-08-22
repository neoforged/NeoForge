/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import java.util.Objects;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A {@link ResourceHandler} that allows extraction of an unlimited amount of a specified resource.
 *
 * @param <T> The type of resource that this storage can accept.
 */
// TODO: questionable usefulness
public class InfiniteResourceHandler<T extends IResource> implements ResourceHandler<T> {
    /**
     * Resource that should be provided infinitely. Mustn't be {@code null}.
     */
    public T infinite;

    public InfiniteResourceHandler(T resource) {
        this.infinite = resource;
    }

    public InfiniteResourceHandler(ResourceStack<T> resourceStack) {
        this(resourceStack.resource());
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0; // doesn't allow insertions
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return resource.equals(infinite) ? amount : 0;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return infinite;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return Long.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return resource.isEmpty();
    }
}
