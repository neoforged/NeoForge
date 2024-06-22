/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

/**
 * A utility interface for a handler that manages a single index of a resource.
 * @param <T> The type of resource this handler manages.
 */
public interface ISingleResourceHandler<T extends IResource> extends IResourceHandler<T> {
    @Override
    default int size() {
        return 1;
    }

    /**
     * @return The resource this handler manages.
     */
    T getResource();

    @Override
    default T getResource(int index) {
        return getResource();
    }

    /**
     * @return The amount of the resource this handler manages.
     */
    int getAmount();

    @Override
    default int getAmount(int index) {
        return getAmount();
    }

    /**
     * Gets the maximum amount that the handler can hold of the given resource.
     *
     * @param resource The resource to get the limit for.
     * @return The limit of the resource.
     */
    int getCapacity(T resource);

    @Override
    default int getCapacity(int index, T resource) {
        return getCapacity(resource);
    }

    /**
     * Gets the theoretical maximum amount that the handler can hold of a resource, regardless of the contents of the handler.
     *
     * @return The limit of the resource.
     */
    int getCapacity();

    @Override
    default int getCapacity(int index) {
        return getCapacity();
    }

    /**
     * Checks if the given resource is valid for insertion into the handler.
     *
     * @param resource The resource to check.
     * @return True if the resource is valid, false otherwise.
     */
    boolean isValid(T resource);

    @Override
    default boolean isValid(int index, T resource) {
        return isValid(resource);
    }

    @Override
    default boolean allowsInsertion(int index) {
        return allowsInsertion();
    }

    @Override
    default boolean allowsExtraction(int index) {
        return allowsExtraction();
    }

    @Override
    boolean allowsInsertion();

    @Override
    boolean allowsExtraction();

    @Override
    default int insert(int index, T resource, int amount, TransferAction action) {
        return insert(resource, amount, action);
    }

    @Override
    default int extract(int index, T resource, int amount, TransferAction action) {
        return extract(resource, amount, action);
    }
}
