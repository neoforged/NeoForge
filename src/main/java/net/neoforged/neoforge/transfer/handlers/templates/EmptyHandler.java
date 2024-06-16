/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

/**
 * An {@link ISingleResourceHandler} that represents a handler that rejects all operations.
 * You should use the static instances {@link #ITEM} and {@link #FLUID} instead of creating new instances.
 * If you're using this with a different resource type, you should create a new static instance.
 *
 * @param <T> The type of resource
 */
public class EmptyHandler<T extends IResource> implements ISingleResourceHandler<T> {
    public static final EmptyHandler<ItemResource> ITEM = new EmptyHandler<>(ItemResource.BLANK);
    public static final EmptyHandler<FluidResource> FLUID = new EmptyHandler<>(FluidResource.BLANK);

    private final T emptyResource;

    public EmptyHandler(T emptyResource) {
        this.emptyResource = emptyResource;
    }

    @Override
    public T getResource() {
        return emptyResource;
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int getLimit(T resource) {
        return 0;
    }

    @Override
    public boolean isValid(T resource) {
        return true;
    }

    @Override
    public boolean canInsert() {
        return false;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return 0;
    }
}
