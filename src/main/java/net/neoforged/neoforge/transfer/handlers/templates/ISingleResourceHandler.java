/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A utility interface for a handler that manages a single index of a resource.
 * The index parameter in the methods from IResourceHandler are expected to be ignored from this interface down. This creates something also referred to as a "Single Index Resource Handler" or a "Slotless Resource Handler" (despite having a single index)
 *
 * @param <T> The type of resource this handler manages.
 */
public interface ISingleResourceHandler<T extends IResource> extends IResourceHandler<T> {
    //Neo: It may be a gut reaction to try to make a proxy method like getResource() or getAmount() that don't take
    //     an index, but that in practice can get rather messy given interfaces cannot have `final` set on the default methods.
    //     If this one day changes in a future java version, then this should be revisited.
    //     For now, let's not clutter the intellisense with misleading method override options
    @Override
    T getResource(int index);

    @Override
    int getAmount(int index);

    @Override
    boolean supportsInsertion();

    @Override
    boolean supportsExtraction();

    @Override
    int getCapacity(int index, T resource);

    @Override
    boolean isValid(int index, T resource);

    /**
     * If you require more than 1 slot, please make sure to create your own implementation of {@link IResourceHandler} instead.
     * This interface is intended as a helper interface and should not mutate the defaulted values. Java doesn't allow {@code final} on interface defaults
     *
     * @return 1 for ISingleResourceHandlers
     */
    @Override
    default int size() {
        //Single resource handlers only have 1 resource thus only really need one index
        return 1;
    }

    @Override
    default int insert(int index, T resource, int amount, TransactionContext context) {
        // With single resource handlers the index is ignored
        return insert(resource, amount, context);
    }

    @Override
    default int extract(int index, T resource, int amount, TransactionContext context) {
        // With single resource handlers the index is ignored
        return extract(resource, amount, context);
    }

    @Override
    default boolean supportsInsertion(int index) {
        //We effectively flip the root's check so that we check on index-less instead
        return supportsInsertion();
    }

    @Override
    default boolean supportsExtraction(int index) {
        //We effectively flip the root's check so that we check on index-less instead
        return supportsExtraction();
    }
}
