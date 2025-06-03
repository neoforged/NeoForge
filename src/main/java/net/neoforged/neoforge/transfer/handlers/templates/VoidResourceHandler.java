/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An {@link ISingleResourceHandler} that automatically destroys any resources that are inserted into it.
 * You should use the static instances provided by {@link #instance} instead of creating one yourself.
 * <p>
 * As an example:
 * 
 * <pre>{@code
 * VoidResourceHandler.instance(ItemResource.EMPTY);
 * }
 * </pre>
 *
 * @param <T> The type of resource that this storage can accept.
 */
public final class VoidResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    //Unlike the Empty handler, since the size is 1, there would be a valid empty resource to support the getResource.
    //This however should be fine to use as an alternative.
    private static final Reference2ReferenceMap<IResource, VoidResourceHandler<?>> instances = new Reference2ReferenceOpenHashMap<>();

    public static <T extends IResource> VoidResourceHandler<T> instance(T emptyResource) {
        if (!emptyResource.isEmpty()) {
            throw new IllegalStateException("Resource must be empty when getting VoidResourceHandler instance");
        }

        //noinspection unchecked
        return (VoidResourceHandler<T>) instances.computeIfAbsent(emptyResource, (iResource) -> new VoidResourceHandler<>((T) iResource));
    }

    private final T emptyResource;

    private VoidResourceHandler(T emptyResource) {
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
        return emptyResource; // The resource type's "None"
    }

    @Override
    public int getAmount(int index) {
        return 0;
    }

    @Override
    public int getCapacity(int index, T resource) {
        return ResourceHandlerUtil.MAX; // Maximum capacity
    }

    @Override
    public boolean isValid(int index, T resource) {
        return true; // What ever resource is queried is always allowed
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return false;
    }
}
