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
    //Single resource handlers only have 1 resource thus only really need one sub-buffer
    @Override
    default int size() {
        return 1;
    }

    @Override
    default boolean isValid(int index, T resource) {
        return isValid(resource);
    }

    @Override
    default int insert(int ignoredIndex, T resource, int amount, TransferAction action) {
        // With single resource handlers the index is ignored
        return insert(resource, amount, action);
    }

    @Override
    default int extract(int ignoredIndex, T resource, int amount, TransferAction action) {
        // With single resource handlers the index is ignored
        return extract(resource, amount, action);
    }

    //These are flipped from the IResource Handler in which defaults into the other,
    // given the indices from the children classes will be ignored.
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

    //Soaryn's notes, These were commented out as adding a sub classed method like getCapacity() winds up only being relevant in those specific use cases.
    // This should likely be generalized, and may be more idealistic to reduce the hierarchy shifts like these. This isn't too different from how Block overrides work now
    // where you have some parameter that is unrelated, you can tend to ignore them for these use cases.
    // I did move the isValid(T resource) up a level, as that could be useful in some uses
    // I am working under the assumption this was to try to reduce down typing, but you may actually be inadvertently increasing confusion.
    // Having the parameter ignored is fine for these cases is fine, and just creates a layer of unnecessary abstraction.

    //    @Override
    //    default T getResource(int index) {
    //        return getResource();
    //    }

    //    @Override
    //    default int getAmount(int index) {
    //        return getAmount();
    //    }

    //    @Override
    //    default int getCapacity(int index, T resource) {
    //        return getCapacity(resource);
    //    }

    //    /**
    //     * @return The resource this handler manages.
    //     */
    //    T getResource();
    //    /**
    //     * @return The amount of the resource this handler manages.
    //     */
    //    int getAmount();

    //    /**
    //     * Gets the maximum amount that the handler can hold of the given resource.
    //     *
    //     * @param resource The resource to get the limit for.
    //     * @return The limit of the resource.
    //     */
    //    int getCapacity(T resource);

    //    /**
    //     * Gets the theoretical maximum amount that the handler can hold of a resource, regardless of the contents of the handler.
    //     *
    //     * @return The limit of the resource.
    //     */
    //    int getCapacity();

    //    @Override
    //    default int getCapacity(int index) {
    //        return getCapacity();
    //    }
}
