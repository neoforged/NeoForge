/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class UnsafeTransactionManager {
    /**
     * Intended to be used when a method will be part of a transaction chain, but the current transaction is not passed in.
     *
     * @return Current transaction on the current thread
     */
    @Nullable
    @ApiStatus.Internal
    public static TransactionContext getCurrentOpenedTransaction() {
        var manager = TransactionManager.getManagerForThread();
        return manager.stack.get(manager.currentDepth);
    }

    private UnsafeTransactionManager() {}
}
