/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.resources.IResource;

public class ResourceHandlerUtil {
    /**
     * A utility method to check both resource and amount to validate if the resource would be empty.
     * <p>
     * Typically used in handler insert or extract implementations to determine if the operation is valid before proceeding.
     *
     * @throws IllegalArgumentException When the amount is negative
     * @see ResourceContainerContentsHandler#insert(int, IResource, int, TransactionContext)
     */
    public static boolean isEmpty(IResource<?> resource, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        return amount == 0 || resource.isEmpty();
    }
}
