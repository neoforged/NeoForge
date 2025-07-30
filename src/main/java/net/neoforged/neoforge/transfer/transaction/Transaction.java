/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * A global operation that guarantees either the whole operation succeeds,
 * or it is completely aborted and rolls back.
 *
 * <p>One can imagine that transactions are like video game checkpoints.
 * <ul>
 * <li>{@linkplain Transaction#open Opening a transaction} with a try-with-resources block creates a checkpoint.</li>
 * <li>Modifications to game state can then happen.</li>
 * <li>Calling {@link #commit} validates the modifications that happened during the transaction,
 * essentially discarding the checkpoint.</li>
 * <li>Calling {@link #close} or doing nothing and letting the transaction be {@linkplain #close closed} at the end
 * of the try-with-resources block cancels any modification that happened during the transaction,
 * reverting to the checkpoint.</li>
 * <li>Calling {@link Transaction#open} with a non-{@code null} parent creates a new nested transaction, i.e. a new checkpoint with the current state.
 * Committing a nested transaction will validate the changes that happened, but they may
 * still be cancelled later if a parent transaction is cancelled.
 * Aborting a nested transaction immediately reverts the changes within that nested transaction - cancelling any modification made after the call
 * to {@link Transaction#open}.</li>
 * </ul>
 *
 * <p>This is illustrated in the following example.
 *
 * <pre>{@code
 * try (Transaction rootTransaction = TransactionManager.open(null)) {
 *     // (A) some transaction operations
 *     try (Transaction nestedTransaction = TransactionManager.open(rootTransaction)) {
 *         // (B) more operations
 *         nestedTransaction.commit();
 *         // Commit the changes that happened in this transaction.
 *         // This is a nested transaction, so changes will only be applied if the root
 *         // transaction is committed too.
 *         // auto-close the transaction when exiting the try block
 *     }
 *     // (C) even more operations
 *     rootTransaction.commit();
 *     // This is a root transaction: changes (A), (B) and (C) are applied.
 *     // auto-close the transaction when exiting the try block
 * }
 * // If we hadn't committed the rootTransaction, all changes (A), (B) and (C) would have been reverted.
 * }</pre>
 *
 * <p>Journals are responsible for upholding this contract themselves, by using {@link SnapshotJournal#onClose}
 * to react to transaction close events and properly validate or revert changes.
 * Any action that modifies the state outside the transaction, such as calls to {@code markDirty()} or neighbor updates,
 * should be deferred until {@linkplain SnapshotJournal#commit() after the root transaction is closed}
 * to give every journal a chance to react to transaction close events.
 *
 * <p>This is very low-level for most applications, and most journals should subclass {@link SnapshotJournal}
 * that will take care of properly maintaining their state.
 *
 * <p>Generally, methods should be passed a {@link TransactionContext} parameter instead of the full {@code Transaction},
 * to make sure they don't accidentally call {@link #commit} or {@link #close}.
 *
 * <p>Every transaction is only valid on the thread it was opened on,
 * and attempts to use it on another thread will throw an exception.
 * Consequently, transactions can be concurrent across multiple threads, as long as they don't share any state.
 */
public final class Transaction implements AutoCloseable, TransactionContext {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Opens a new transaction with a specified parent. The example below, we open the outermost layer or the `root`.
     *
     * <pre>
     * {@code
     * try (var transaction = TransactionManager.open(null)) {
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
        return TransactionManager.getManagerForThread().internalOpen(parent, STACK_WALKER.getCallerClass());
    }

    /**
     * @return The current lifecycle of the transaction stack on this thread.
     */
    public static Lifecycle getLifecycle() {
        TransactionManager transactionManager = TransactionManager.getManagerForThread();
        return transactionManager.currentDepth == -1 ? Lifecycle.NONE : transactionManager.stack.get(transactionManager.currentDepth).lifecycle;
    }

    /**
     * @return True if a transaction is open or closing on the current thread, and false otherwise.
     */
    public static boolean isActive() {
        return getLifecycle() != Lifecycle.NONE;
    }

    /**
     * Intended to be used when a method will be part of a transaction chain, but the current transaction
     * is not passed in with no way to change the method signature.
     *
     * @return Current transaction on the current thread
     * @deprecated Only intended to be used in the case you don't have the transaction context in the method you are in,
     *             while expecting a transaction to be open already.
     */
    @Nullable
    @Deprecated
    public static TransactionContext getCurrentOpenedTransaction() {
        TransactionManager manager = TransactionManager.getManagerForThread();
        if (manager.currentDepth == -1) return null;
        return manager.stack.get(manager.currentDepth);
    }

    /**
     * Close the current transaction, committing all the changes that happened during this transaction and its <b>committed</b> child transactions.
     * If this transaction was opened with a {@code null} parent, all changes are applied.
     * If this transaction was opened with a non-{@code null} parent, all changes will be applied when and if the changes of
     * the parent transactions are applied.
     *
     * @throws IllegalStateException If this function is not called on the thread this transaction was opened in,
     *                               this transaction is not the current transaction, or this transaction was closed.
     */
    public void commit() {
        close(Result.COMMITTED);
    }

    /**
     * Abort the current transaction if it was not closed already.
     */
    @Override
    public void close() {
        // check that a transaction is open on this thread and that this transaction is open.
        if (manager.isOpen() && lifecycle == Lifecycle.OPEN) {
            close(Result.ABORTED);
        }
    }

    @Override
    public int depth() {
        validateCurrentThread();
        return depth;
    }

    @Override
    public Transaction getOpenTransaction(int depth) {
        validateCurrentThread();

        if (depth < 0) {
            throw new IndexOutOfBoundsException("Nesting depth may not be negative.");
        }

        if (depth > manager.currentDepth) {
            throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + depth);
        }

        Transaction transaction = manager.stack.get(depth);
        transaction.validateOpen();
        return transaction;
    }

    void addCloseCallback(SnapshotJournal<?> closeCallback) {
        validateCurrentThread();
        validateOpen();
        closeCallbacks.add(closeCallback);
    }

    void addRootCloseCallback(SnapshotJournal<?> journal) {
        validateCurrentThread();
        // Note: we don't call validateOpen() because this transaction may not be open if this is called during a CloseCallback.
        // We rely on a currentDepth check instead, as the depth is only set to -1 at the very end of close(Result).

        if (manager.currentDepth == -1) {
            throw new IllegalStateException("There is no open transaction on this thread.");
        }

        manager.closeableJournals.add(journal);
    }

    @Override
    public String toString() {
        return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(depth, lifecycle.name(), manager.thread.getName());
    }

    //Internal handling
    Lifecycle lifecycle = Lifecycle.NONE;

    private final TransactionManager manager;
    private final int depth;
    private final List<SnapshotJournal<?>> closeCallbacks = new ArrayList<>();

    //Package protected constructor
    Transaction(TransactionManager manager, int depth) {
        this.manager = manager;
        this.depth = depth;
    }

    void validateCurrentThread() {
        if (Thread.currentThread() != manager.thread) {
            String errorMessage = String.format(
                    "Attempted to access transaction state from thread %s, but this transaction is only valid on thread %s.",
                    Thread.currentThread().getName(),
                    manager.thread.getName());
            throw new IllegalStateException(errorMessage);
        }
    }

    void validateCurrentTransaction() {
        validateCurrentThread();

        if (manager.currentDepth != -1 && manager.stack.get(manager.currentDepth) == this)
            return;

        String self = TransactionManager.debugNameFrom(manager.debugMap.get(depth));
        String actual = TransactionManager.debugNameFrom(manager.debugMap.get(manager.currentDepth));

        String errorMessage = String.format(
                "Transaction function was called on a transaction (%s) with depth `%d`, " +
                        "but the current transaction (%s) has depth `%d`.",
                actual,
                depth,
                self,
                manager.currentDepth);
        throw new IllegalStateException(errorMessage);
    }

    // Validate that this transaction is open.
    void validateOpen() {
        if (lifecycle != Lifecycle.OPEN) {
            throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction.");
        }
    }

    private void close(Result result) {
        validateCurrentTransaction();
        validateOpen();
        // Block transaction operations
        lifecycle = Lifecycle.CLOSING;

        // Note: it is important that we don't let exceptions corrupt the global state of the transaction manager.
        // That is why every callback has to run inside its own try-with-resources block.
        RuntimeException closeException = null;

        // Invoke callbacks in reverse order
        for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
            try {
                closeCallbacks.get(i).onClose(this, result.wasAborted());
            } catch (Exception exception) {
                if (closeException == null) {
                    closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
                } else {
                    closeException.addSuppressed(exception);
                }
            }
        }

        closeCallbacks.clear();

        if (manager.currentDepth == 0) {
            lifecycle = Lifecycle.ROOT_CLOSING;

            // Invoke close callbacks in reverse order
            for (int i = manager.closeableJournals.size() - 1; i >= 0; i--) {
                try {
                    manager.closeableJournals.get(i).commit();
                } catch (Exception exception) {
                    if (closeException == null) {
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction root close callback.", exception);
                    } else {
                        closeException.addSuppressed(exception);
                    }
                }
            }

            manager.closeableJournals.clear();
        }

        // Only this check will allow openOuter operations.
        manager.currentDepth--;
        lifecycle = Lifecycle.NONE;

        // Throw exception if necessary
        if (closeException != null) {
            throw closeException;
        }
    }

    /**
     * The result of a transaction operation.
     */
    private enum Result {
        ABORTED,
        COMMITTED;

        /**
         * @return true if the transaction was aborted, false if it was committed.
         */
        public boolean wasAborted() {
            return this == ABORTED;
        }
    }
}
