/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

/**
 * An {@link IResourceHandler} that represents a handler that rejects all operations.
 * You should use the static instances {@link #ITEM} and {@link #FLUID} instead of creating new instances.
 * If you're using this with a different resource type, you should create a new static instance.
 *
 * @param <T> The type of resource
 */
public record EmptyHandler<T extends IResource>(T emptyResource) implements IResourceHandler<T> {
    public static final EmptyHandler<ItemResource> ITEM = new EmptyHandler<>(ItemResource.NONE);
    public static final EmptyHandler<FluidResource> FLUID = new EmptyHandler<>(FluidResource.NONE);

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public T getResource(int index) {
        return emptyResource;
    }

    @Override
    public int getAmount(int index) {
        return 0;
    }

    @Override
    public int getCapacity(int index) {
        return 0;
    }

    @Override
    public int getCapacity(int index, T resource) {
        return 0;
    }

    @Override
    public boolean isValid(int index, T resource) {
        return false;
    }

    @Override
    public boolean allowsInsertion(int index) {
        return false;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return false;
    }
}
