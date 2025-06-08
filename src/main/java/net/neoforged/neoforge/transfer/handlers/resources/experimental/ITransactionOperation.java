/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources.experimental;

import net.neoforged.neoforge.transfer.transaction.Transaction;

// Note to Orion, still working out how this would work and whether or not it is possible. I've tagged this and a few other things so I can quick remove if needed

// PROTOTYPE

@FunctionalInterface
public interface ITransactionOperation<T extends ITransactionHandler> {
    ITransactionOperation<?> EMPTY = (handler, transaction) -> {};

    void run(T previousHandler, Transaction transaction);

    default <TNext extends ITransactionHandler> ITransactionOperation<?> whenSuccessful(TNext nextHandler, ITransactionOperation<TNext> next) {
        return (handler, transaction) -> {
            try (var innerTransaction = Transaction.open(transaction)) {
                Reporter reporter = transaction.reporting();

                run((T) handler, innerTransaction);
                if (!reporter.isSuccess()) return;
            }
            next.run(nextHandler, transaction);
        };
    }

    default <TNext extends ITransactionHandler> ITransactionOperation<?> next(TNext nextHandler, ITransactionOperation<TNext> next) {
        return (handler, transaction) -> {
            run((T) handler, transaction);
            next.run(nextHandler, transaction);
        };
    }

    default <TNext extends ITransactionHandler> ITransactionOperation<?> whenNotCommitted(TNext nextHandler, ITransactionOperation<TNext> next) {
        return (handler, transaction) -> {
            try (var innerTransaction = Transaction.open(transaction)) {
                Reporter reporter = transaction.reporting();

                run((T) handler, innerTransaction);
                if (reporter.isSuccess()) return;
            }
            next.run(nextHandler, transaction);
        };
    }

    static <T extends ITransactionHandler> ITransactionOperation<T> begin() {
        return (ITransactionOperation<T>) EMPTY;
    }
}
