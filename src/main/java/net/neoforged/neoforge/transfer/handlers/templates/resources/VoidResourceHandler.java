/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import java.util.Objects;

import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A resource handler that automatically destroys any resources that are inserted into it.
 * You should use the static instances provided such as {@link #ITEM} or {@link #FLUID} instead of creating one yourself.
 * In the case of your own resource type, or one not specifically builtin, it is advised to create your own cached instance for reuse.
 *
 * @param <T> The type of resource that this storage can accept.
 */
public final class VoidResourceHandler<T extends IResource> implements IResourceHandler<T> {
    public static final VoidResourceHandler<ItemResource> ITEM = new VoidResourceHandler<>(ItemResource.EMPTY);
    public static final VoidResourceHandler<FluidResource> FLUID = new VoidResourceHandler<>(FluidResource.EMPTY);

    private final T emptyResource;

    public VoidResourceHandler(T emptyResource) {
        this.emptyResource = emptyResource;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        // Always accept the full amount
        return amount;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return emptyResource;
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        return Integer.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        Objects.checkIndex(index, size());
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return true;
    }
}
