/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.ApiStatus;

/**
 * A subset of a {@link Transaction} that lets journals properly record, manage their state,
 * or open inner transactions, but does not allow them to close the transaction they are passed.
 * <p>
 * You should never need to cast this to a {@link Transaction}.
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
     *
     * {@return the current lifecycle of this transaction}
     * 
     * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
     */
    Lifecycle lifecycle();

    /**
     * {@return True if this transaction is open or closing, and false otherwise}
     * 
     * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
     */
    @ApiStatus.NonExtendable
    default boolean isActive() {
        return lifecycle().isActive();
    }

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
        ROOT_CLOSING;

        /**
         * Indicates if there is any activity in the life cycle. In other words anything other than {@link #NONE}
         * 
         * @return {@code true} if is open or closing, and {@code false} otherwise.
         */
        boolean isActive() {
            return this != NONE;
        }

        boolean isOpen() {
            return this == OPEN;
        }
    }
}
