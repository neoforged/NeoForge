/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.resources.IResource;

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
