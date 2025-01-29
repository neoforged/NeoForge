/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * A utility interface for a handler that manages a single index of a resource.
 * The index parameter in the methods from IResourceHandler are expected to be ignored from this interface down. This creates something also referred to as a `Slot-lessResourceHandler`
 * 
 * @param <T> The type of resource this handler manages.
 */
public interface ISingleResourceHandler<T extends IResource> extends IResourceHandler<T> {
    //Single resource handlers only have 1 resource thus only really need one index
    @Override
    default int size() {
        return 1;
    }

    @Override
    default int insert(int index, T resource, int amount, TransferAction action) {
        // With single resource handlers the index is ignored
        return insert(resource, amount, action);
    }

    @Override
    default int extract(int index, T resource, int amount, TransferAction action) {
        // With single resource handlers the index is ignored
        return extract(resource, amount, action);
    }

    @Override
    T getResource(int index);

    //These allow methods are flipped from the IResourceHandler for which one is the default and which needs to be implemented.
    @Override
    boolean allowsInsertion();

    @Override
    default boolean allowsInsertion(int index) {
        return allowsInsertion();
    }

    @Override
    boolean allowsExtraction();

    @Override
    default boolean allowsExtraction(int index) {
        return allowsExtraction();
    }

    @Override
    int getAmount(int ignoredIndex);

    @Override
    int getCapacity(int ignoredIndex);

    @Override
    int getCapacity(int index, T resource);

    @Override
    boolean isValid(int index, T resource);
}
