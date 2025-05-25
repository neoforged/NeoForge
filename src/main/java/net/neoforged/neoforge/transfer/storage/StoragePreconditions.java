/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.storage;

/**
 * Preconditions that can be used when working with storages.
 *
 * <p>In particular, {@link #notNegative} or {@link #notBlankNotNegative} can be used by implementations of
 * {@link Storage#insert} and {@link Storage#extract} to fail-fast if the arguments are invalid.
 */
public final class StoragePreconditions {
//    /**
//     * Ensure that the passed transfer variant is not blank.
//     *
//     * @throws IllegalArgumentException If the variant is blank.
//     */
//    public static void notBlank(TransferVariant<?> variant) {
//        if (variant.isBlank()) {
//            throw new IllegalArgumentException("Transfer variant may not be blank.");
//        }
//    }
    /**
     * Ensure that the passed amount is not negative. That is, it must be {@code >= 0}.
     *
     * @throws IllegalArgumentException If the amount is negative.
     */
    public static void notNegative(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount may not be negative, but it is: " + amount);
        }
    }

//    /**
//     * Check both for a not blank transfer variant and a not negative amount.
//     */
//    public static void notBlankNotNegative(TransferVariant<?> variant, long amount) {
//        notBlank(variant);
//        notNegative(amount);
//    }

    private StoragePreconditions() {}
}
