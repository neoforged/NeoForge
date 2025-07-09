/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.resources.IResource;

public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {}

    /**
     * Determines if either the given resource or amount is classified as empty.
     *
     * @param resource The resource to check.
     * @param amount   An amount to check. <strong>Must be non-negative.</strong>
     * @return {@code true} if either {@link IResource#isEmpty()} returns true, or the amount is zero.
     */
    public static boolean isEmpty(IResource<?> resource, int amount) {
        return amount == 0 || resource.isEmpty();
    }
}
