/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

public class TransferPreconditions {
    private TransferPreconditions() {}

    /**
     * Ensures the value passed in is non-negative, throws otherwise.
     *
     * @return The value passed.
     * @throws IllegalArgumentException when value is negative.
     */
    public static int checkNonNegative(int value) {
        if (value >= 0) return value;
        throw new IllegalArgumentException("Non-negative check failed: " + value);
    }
}
