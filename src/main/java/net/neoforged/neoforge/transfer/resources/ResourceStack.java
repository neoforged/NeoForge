/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;

/**
 * Creates a resource stack from a given {@link IResource resource} and {@code amount}.
 * 
 * @param resource The resource to wrap the stack around.
 * @param amount   The amount of the resource the stack is holding. Must be non-negative. Should only be 0 if the resource isEmpty
 *
 */
public record ResourceStack<T extends IResource>(T resource, int amount) {
    /**
     * Checks if the resource stack is empty, meaning that the amount is zero
     * or that the resource is {@link IResource#isEmpty() empty}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return ResourceHandlerUtil.isEmpty(resource(), amount());
    }

    @Override
    public String toString() {
        return resource + "(" + amount + ")";
    }
}
