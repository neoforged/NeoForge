/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.resources.IResource;

public class TransferPreconditions {
    private TransferPreconditions() {}

    /**
     * Ensures the value passed in is non-negative, throws otherwise.
     *
     * @throws IllegalArgumentException when value is negative.
     */
    public static void checkNonNegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Expected value to be non-negative: " + value);
        }
    }

    /**
     * Ensures the resource passed in is non-empty and the value passed in is non-negative, throws otherwise.
     *
     * @throws IllegalArgumentException when resource is empty or value is negative.
     */
    public static void checkNonEmptyNonBlank(IResource resource, int value) {
        checkNonNegative(value);
        if (resource.isEmpty()) {
            throw new IllegalArgumentException("Expected resource to be non-empty: " + resource);
        }
    }
}
