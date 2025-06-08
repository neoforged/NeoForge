/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources.experimental;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

// PROTOTYPE
public interface ITransactionHandler {
    default <T extends ITransactionHandler> void operate(TransactionContext transaction, ITransactionOperation<T> operation) {
        try (var inner = Transaction.open(transaction)) {
            operation.run((T) this, inner);
        }
    }
}
