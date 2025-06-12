/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * A generic handler for handling a {@link IResource resource} of type {@link T} allowing for direct mutations of a specific slot.
 * It is advised to avoid calling {@link #set} on handlers that are not your own.
 *
 * @param <T> The type of {@link IResource resource} this handler manages.
 */
public interface IResourceHandlerModifiable<T extends IResource> extends IResourceHandler<T> {
    /**
     * Sets the resource and amount at the given index to the given resource and amount. This bypasses all validation methods. This is intended for more internal use or testing specific scenarios.
     *
     * @param index    The index to set the resource at.
     * @param resource The resource to set.
     * @param amount   The amount of the resource to set.
     */
    void set(int index, T resource, int amount);
}
