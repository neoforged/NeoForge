/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.ApiStatus;

/**
 * A subset of a {@link Transaction} that lets journals properly record in transactions, manage their state,
 * or open inner transactions, but does not allow them to close the transaction they are passed.
 * <p>
 * You should never cast this to a {@link Transaction}.
 */
@ApiStatus.NonExtendable
public interface TransactionContext {
    /**
     * Gets the current depth of the transaction.
     * 
     * @return The depth of this transaction: 0 if it is the root and has no parent; 1 or more otherwise indicating how far away from the root the transaction is.
     * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
     */
    int depth();

    /**
     * Return the transaction with the specific nesting depth.
     *
     * @param depth Queried nesting depth.
     * @throws IndexOutOfBoundsException If there is no open transaction with the request nesting depth.
     * @throws IllegalStateException     If this function is not called on the thread this transaction was opened in.
     */
    Transaction getOpenTransaction(int depth);

    enum Lifecycle {
        /**
         * No transaction is currently open or closing.
         */
        NONE,
        /**
         * A transaction is currently open.
         */
        OPEN,
        /**
         * The current transaction is invoking its close callbacks.
         */
        CLOSING,
        /**
         * The current transaction is invoking its root close callbacks.
         */
        ROOT_CLOSING
    }
}
