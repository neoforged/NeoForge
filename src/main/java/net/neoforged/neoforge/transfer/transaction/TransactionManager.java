/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.Nullable;

/**
 * Manager for handling opening new {@link Transaction Transactions} or querying status of a transaction chain in a given thread.
 * 
 * @see Transaction
 */
public final class TransactionManager {
    /**
     * Opens a new transaction from the root.
     *
     * <pre>
     * {@code
     * try (var transaction = TransactionManager.open()) {
     *     // do exchanges
     * }
     * }</pre>
     *
     * @throws IllegalStateException A root transaction is already active on the current thread.
     */
    public static Transaction open() {
        return TransactionManagerImpl.MANAGERS.get().open(null);
    }

    /**
     * Opens a new transaction with a specified parent. The example below, we open the outermost layer or the `root`.
     *
     * <pre>
     * {@code
     * try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
     *     // do exchanges
     * }
     * }</pre>
     *
     * @param parent the parent transaction, or null if this is the root transaction
     * @throws IllegalStateException If no parent is passed, but a transaction is already active on the current thread.
     * @throws IllegalStateException If a parent is passed, but it's not the current transaction.
     * @throws IllegalStateException If a parent is passed, but it was already closed.
     */
    public static Transaction open(@Nullable TransactionContext parent) {
        return TransactionManagerImpl.MANAGERS.get().open(parent);
    }

    /**
     * @return The current lifecycle of the transaction stack on this thread.
     */
    public static Transaction.Lifecycle getLifecycle() {
        return TransactionManagerImpl.MANAGERS.get().getLifecycle();
    }

    /**
     * @return True if a transaction is open or closing on the current thread, and false otherwise.
     */
    public static boolean isActive() {
        return getLifecycle() != Transaction.Lifecycle.NONE;
    }

    private TransactionManager() {}
}
