/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for handling opening new {@link Transaction Transactions} or querying status of a transaction chain in a given thread.
 *
 * @see Transaction
 */
final class TransactionManager {
    private static final ThreadLocal<TransactionManager> MANAGERS = ThreadLocal.withInitial(TransactionManager::new);
    final Thread thread = Thread.currentThread();
    final List<Transaction> stack = new ArrayList<>();
    final List<SnapshotJournal<?>> journalsToCommitWithRoot = new ArrayList<>();
    int currentDepth = -1;

    boolean isOpen() {
        return currentDepth > -1;
    }

    /**
     * @return The manager for the current thread.
     */
    static TransactionManager getManagerForThread() {
        return MANAGERS.get();
    }

    Transaction open(@Nullable TransactionContext parent, Class<?> callerClass) {
        if (parent != null) {
            Transaction parentImpl = (Transaction) parent;
            validateCurrentTransaction(parentImpl);
            parentImpl.validateOpen();
        } else if (isOpen()) {
            //get the root's name
            String currentRoot = getOpenTransaction(0).getDebugName();
            throw new IllegalStateException("A root transaction of `" + currentRoot + "` is already active on this thread " + thread + " when `" + callerClass + "` tried to open.");
        }

        Transaction current;
        if (stack.size() == ++currentDepth) {
            current = new Transaction(this, currentDepth, callerClass);
            stack.add(current);
        } else {
            current = stack.get(currentDepth);
        }
        current.lifecycle = Transaction.Lifecycle.OPEN;
        return current;
    }

    /**
     * Return the transaction with the specified depth.
     *
     * @param depth Queried depth of the transaction desired.
     * @throws IndexOutOfBoundsException If there is no open transaction with the requested depth.
     * @throws IllegalStateException     If this function is not called on the thread this transaction was opened in.
     */
    Transaction getOpenTransaction(int depth) {
        validateCurrentThread();

        if (depth < 0) {
            throw new IndexOutOfBoundsException("Depth may not be negative.");
        }

        if (depth > this.currentDepth) {
            throw new IndexOutOfBoundsException("There is no open transaction for depth `" + depth + "`");
        }

        Transaction transaction = this.stack.get(depth);
        transaction.validateOpen();
        return transaction;
    }

    void validateCurrentThread() {
        if (Thread.currentThread() != thread) {
            String errorMessage = String.format(
                    "Attempted to access transaction state from thread %s, but this transaction is only valid on thread %s.",
                    Thread.currentThread().getName(),
                    thread.getName());
            throw new IllegalStateException(errorMessage);
        }
    }

    void validateCurrentTransaction(Transaction transaction) {
        validateCurrentThread();

        if (currentDepth != -1 && stack.get(currentDepth) == transaction)
            return;

        String self = transaction.getDebugName();
        String actual = getOpenTransaction(currentDepth).getDebugName();

        String errorMessage = String.format(
                "Transaction function was called on a transaction (%s) with depth `%d`, " +
                        "but the current transaction (%s) has depth `%d`.",
                actual,
                transaction.depth(),
                self,
                currentDepth);
        throw new IllegalStateException(errorMessage);
    }

    private TransactionManager() {}
}
