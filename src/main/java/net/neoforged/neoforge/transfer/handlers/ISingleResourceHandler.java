/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

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
     * Gets the limit that the resource can hold. If you'd like to get the theoretical limit of what
     * the handler can hold, pass in a blank resource.
     *
     * @param resource The resource to get the limit for.
     * @return The limit of the resource.
     */
    int getLimit(T resource);

    @Override
    default int getLimit(int index, T resource) {
        return getLimit(resource);
    }

    boolean isValid(T resource);

    @Override
    default boolean isValid(int index, T resource) {
        return isValid(resource);
    }

    @Override
    default int insert(int index, T resource, int amount, TransferAction action) {
        return insert(resource, amount, action);
    }

    @Override
    default int extract(int index, T resource, int amount, TransferAction action) {
        return extract(resource, amount, action);
    }
}
