/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

/**
 * Unsafe utility methods that are advised against using outside of Neo.
 * There are some cases where you may need the current transaction context
 * while inside a method that was not passed the context. If you can, it is
 * more ideal to modify the method signature to allow passing the existing
 * context around.
 * <p>
 * In short, <strong>use this as a last resort</strong>.
 */
@Deprecated
public final class UnsafeTransactionManager {
    /**
     * It is advised to, when possible, use the provided {@link TransactionContext} in the method you are in.
     * If you have control of the method signature, you should do your best to pass the context through it rather than
     * use this method as a "lazy" way to open transactions.
     * <p>
     * As the class name would suggest, this is <strong>unsafe</strong>.
     *
     * @return A new transaction using the current transaction for the thread.
     */
    @Deprecated
    public static Transaction openUnsafe() {
        return Transaction.open(Transaction.getCurrentOpenedTransaction());
    }

    private UnsafeTransactionManager() {}
}
