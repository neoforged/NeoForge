/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import java.util.Objects;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A resource handler that destroys any resources inserted into it.
 *
 * @param <T> The type of resource that this handler can accept.
 */
public final class VoidingResourceHandler<T extends IResource> implements ResourceHandler<T> {
    private final T emptyResource;

    /**
     * @param emptyResource The resource to return when the contents of this handler are queried.
     */
    public VoidingResourceHandler(T emptyResource) {
        this.emptyResource = emptyResource;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        // Always accept the full amount
        return amount;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return emptyResource;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return 0;
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
