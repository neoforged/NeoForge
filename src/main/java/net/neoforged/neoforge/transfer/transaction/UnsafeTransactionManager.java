/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Unsafe utility methods that are advised against using outside of Neo.
 * There are some cases where you may need the current transaction context
 * while inside a method that was not passed the context. If you can, it is
 * more ideal to modify the method signature to allow passing the existing
 * context around.
 * <p>
 * In short, <strong>use this as a last resort</strong>.
 */
@ApiStatus.Internal
public final class UnsafeTransactionManager {
    /**
     * Intended to be used when a method will be part of a transaction chain, but the current transaction
     * is not passed in with no way to change the method signature.
     *
     * @return Current transaction on the current thread
     */
    @Nullable
    @ApiStatus.Internal
    public static TransactionContext getCurrentOpenedTransaction() {
        var manager = TransactionManager.getManagerForThread();
        if (manager.currentDepth == -1) return null;
        return manager.stack.get(manager.currentDepth);
    }

    /**
     * It is advised to, when possible, use the provided {@link TransactionContext} in the method you are in.
     * If you have control of the method signature, you should do your best to pass the context through it rather than
     * use this method as a "lazy" way to open transactions.
     * <p>
     * As the class name would suggest, this is <strong>unsafe</strong>.
     *
     * @return A new transaction using the current transaction for the thread.
     */
    @ApiStatus.Internal
    public static Transaction openUnsafe() {
        return TransactionManager.open(getCurrentOpenedTransaction());
    }

    private UnsafeTransactionManager() {}
}
