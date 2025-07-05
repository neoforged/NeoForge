/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import java.util.Objects;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An {@link ISingleResourceHandler} that automatically destroys any resources that are inserted into it.
 * You should use the static instances provided such as {@link #ITEM} or {@link #FLUID} instead of creating one yourself.
 * In the case of your own resource type, or one not specifically builtin, it is advised to create your own cached instance for reuse.
 *
 * @param <T> The type of resource that this storage can accept.
 */
public final class VoidResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    public static final VoidResourceHandler<ItemResource> ITEM = new VoidResourceHandler<>(ItemResource.EMPTY);
    public static final VoidResourceHandler<FluidResource> FLUID = new VoidResourceHandler<>(FluidResource.EMPTY);

    private final T emptyResource;

    public VoidResourceHandler(T emptyResource) {
        this.emptyResource = emptyResource;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext context) {
        return amount; // ignore the inputs, and inform the return is always accepted
    }

    @Override
    public int extract(T resource, int amount, TransactionContext context) {
        return 0; // Nothing is ever allowed to be extracted
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return emptyResource; // The resource type's "None"
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return 0;
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
        return true; // What ever resource is queried is always allowed
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.INSERTABLE | TransferCharacteristics.VOIDING | TransferCharacteristics.IMMUTABLE;
    }
}
