/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources.experimental;

import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
// PROTOTYPE

public final class Reporter implements Transaction {
    private final TransactionContext transaction;

    private boolean success;

    public Reporter(TransactionContext transaction) {
        this.transaction = transaction;
    }

    @Override
    public void commit() {
        success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public void close() {}

    @Override
    public int nestingDepth() {
        return transaction.nestingDepth();
    }

    @Override
    public Transaction getOpenTransaction(int nestingDepth) {
        return transaction.getOpenTransaction(nestingDepth);
    }

    @Override
    public void addCloseCallback(CloseCallback closeCallback) {
        transaction.addCloseCallback(closeCallback);
    }

    @Override
    public void addRootCloseCallback(RootCloseCallback rootCloseCallback) {
        transaction.addRootCloseCallback(rootCloseCallback);
    }
}
