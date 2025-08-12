/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.resources.IResource;

public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {}

    /**
     * Determines if either the given resource or amount is classified as empty: if either {@link IResource#isEmpty()} is {@code true},
     * or the amount is zero (or negative) then the resource is considered empty.
     *
     * @param resource The resource to check.
     * @param amount   An amount to check.
     * @return {@code true} if either {@link IResource#isEmpty()} returns {@code true}, or the amount is {@code <= 0}.
     */
    public static boolean isEmpty(IResource resource, int amount) {
        return amount <= 0 || resource.isEmpty();
    }
}
