/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resources;

import java.util.Objects;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A resource handler that automatically destroys any resources that are inserted into it.
 *
 * @param <T> The type of resource that this handler can accept.
 */
public final class VoidResourceHandler<T extends IResource> implements IResourceHandler<T> {
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
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);
        // Always accept the full amount
        return amount;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);
        return 0;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        return emptyResource;
    }

    @Override
    public long getAmount(int index) {
        Objects.checkIndex(index, size());
        return 0;
    }

    @Override
    public long getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        return true;
    }
}
