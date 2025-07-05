/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.handlers.wrappers.items.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * Provides information on how a particular container would be mutated at a given index,
 * a resource, and an amount. This is intended to be used with {@link ResourceHandlerSlot}
 * instead of making an IModifiableResourceHandler.
 * 
 * @param <T>
 */
@FunctionalInterface
public interface IIndexModifier<T extends IResource> {
    /**
     * Overrides the resource and amount at the given index.
     *
     * @param index    The index to set the resource at.
     * @param resource The resource to set.
     * @param amount   The amount of the resource to set.
     */
    void set(int index, T resource, int amount);
}
