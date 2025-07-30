/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

/**
 * This is left in purely as a helper to inline uses in later slices, this is not being kept in the full branch.
 * Earliest ideal scenario to remove is likely after the vanilla implementations/wrappers. Absolute latest is when we start the Templates
 */
@Deprecated(forRemoval = true)
public final class UnsafeTransactionManager {
    @Deprecated(forRemoval = true)
    public static Transaction openUnsafe() {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }

    private UnsafeTransactionManager() {}
}
