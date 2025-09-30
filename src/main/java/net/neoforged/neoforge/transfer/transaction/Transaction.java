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
 * Generally when passed to a method, a {@link Transaction} should be passed as a {@link TransactionContext} to prevent misuse such as calling {@link #commit()} or {@link #close()}
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
 * <li>Calling {@link Transaction#open} with a non-{@code null} parent creates a new inner transaction, i.e. a new checkpoint with the current state.
 * Committing an inner transaction will validate the changes that happened, but they may
 * still be cancelled later if a parent transaction is cancelled.
 * Aborting an inner transaction immediately reverts the changes within that inner transaction - cancelling any modification made after the call
 * to {@link Transaction#open}.</li>
 * </ul>
 *
 * <p>This is illustrated in the following example.
 *
 * <pre>{@code
 * try (Transaction rootTransaction = TransactionManager.open(null)) {
 *     // (A) some transaction operations
 *     try (Transaction innerTransaction = TransactionManager.open(rootTransaction)) {
 *         // (B) more operations
 *         innerTransaction.commit();
 *         // Commit the changes that happened in this transaction.
 *         // This is an inner transaction, so changes will only be applied if the root
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
 * <p>Journals are responsible for upholding this contract themselves, by using {@link SnapshotJournal#close}
 * to react to transaction close events and properly validate or revert changes.
 * Any action that modifies the state outside the transaction, such as calls to {@code markDirty()} or neighbor updates,
 * should be deferred until {@linkplain SnapshotJournal#onRootCommit(Object) after the root transaction is closed}
 * to give every journal a chance to react to transaction close events.
 *
 * <p>This is very low-level for most applications, and most journals should subclass {@link SnapshotJournal}
 * that will take care of properly maintaining their state.
 *
 * <p>Every transaction is only valid on the thread it was opened on,
 * and attempts to use it on another thread will throw an exception.
 * Consequently, transactions can be concurrent across multiple threads, as long as they don't share any state.
 */
public final class Transaction implements AutoCloseable, TransactionContext {
    /**
     * Stack walker to provide a name for the opener of the transaction. This is used for debugging purposes such as {@link TransactionManager#validateCurrentTransaction}.
     */
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
        return TransactionManager.getManagerForThread().open(parent, STACK_WALKER.getCallerClass());
    }

    /**
     * {@return The current lifecycle of the transaction stack on this thread.}
     */
    public static Lifecycle getLifecycle() {
        TransactionManager manager = TransactionManager.getManagerForThread();
        return manager.currentDepth == -1 ? Lifecycle.NONE : manager.stack.get(manager.currentDepth).lifecycle();
    }

    /**
     * Indicates if there is an active transaction on the current thread.
     *
     * @return {@code true} if a transaction is open or closing on the current thread, and {@code false} otherwise.
     */
    public static boolean hasActiveTransaction() {
        return getLifecycle().isActive();
    }

    /**
     * Intended to be used when a method will be part of a transaction chain, but the current transaction
     * is not passed in with no way to change the method signature.
     *
     * @return Current {@link Transaction} on the current thread
     * @deprecated Only intended to be used in the case you don't have the transaction context in the method you are in,
     *             while expecting a transaction to be open already.
     *             If you have access to a transaction context already, be sure to use that rather than using this method.
     * @throws IllegalStateException when called while a transaction is closing.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Nullable
    @Deprecated
    public static TransactionContext getCurrentOpenedTransaction() {
        TransactionManager manager = TransactionManager.getManagerForThread();
        // This should also handle the case of LifeCycle is NONE without having to explicitly check
        if (manager.currentDepth == -1) return null;

        Transaction transaction = manager.stack.get(manager.currentDepth);
        if (transaction.lifecycle.isOpen()) return transaction;
        // The life cycle is either CLOSING or ROOT_CLOSING
        throw new IllegalStateException("`getCurrentOpenedTransaction()` cannot be called while a transaction is closing.");
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
        if (manager.isOpen() && lifecycle.isOpen()) {
            close(Result.ABORTED);
        }
    }

    @Override
    public int depth() {
        manager.validateCurrentThread();
        return depth;
    }

    public void addClosingJournal(SnapshotJournal<?> journal) {
        manager.validateCurrentThread();
        validateOpen();
        journalsToClose.add(journal);
    }

    public void addClosingJournalToPrevDepth(SnapshotJournal<?> journal) {
        manager.getOpenTransaction(depth - 1).addClosingJournal(journal);
    }

    public void addCommittingJournal(SnapshotJournal<?> journal) {
        manager.validateCurrentThread();
        // Note: we don't call validateOpen() because this transaction may not be open if this is called during a CloseCallback.
        // We rely on a currentDepth check instead, as the depth is only set to -1 at the very end of close(Result).

        if (manager.currentDepth == -1) {
            throw new IllegalStateException("There is no open transaction on this thread.");
        }

        manager.journalsToCommitWithRoot.add(journal);
    }

    @Override
    public String toString() {
        return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(depth, lifecycle.name(), manager.thread.getName());
    }

    // Internals
    Lifecycle lifecycle = Lifecycle.NONE;

    private final TransactionManager manager;
    private final int depth;
    private final List<SnapshotJournal<?>> journalsToClose = new ArrayList<>();
    private final Class<?> callerClass;

    // Package protected constructor
    Transaction(TransactionManager manager, int depth, Class<?> callerClass) {
        this.manager = manager;
        this.depth = depth;
        this.callerClass = callerClass;
    }

    // Validate that this transaction is open.
    void validateOpen() {
        if (!lifecycle.isOpen()) {
            throw new IllegalStateException("Transaction operation cannot be applied to a closed or closing transaction.");
        }
    }

    Lifecycle lifecycle() {
        manager.validateCurrentThread();
        return lifecycle;
    }

    /**
     * Gets what should be printed during exceptions to represent the caller class. This should include the package name as well.
     */
    String getDebugName() {
        return callerClass.toString();
    }

    private void close(Result result) {
        manager.validateCurrentTransaction(this);
        validateOpen();
        // Block transaction operations
        lifecycle = Lifecycle.CLOSING;

        // Note: it is important that we don't let exceptions corrupt the global state of the transaction manager.
        // That is why every callback has to run inside its own try-with-resources block.
        RuntimeException closeException = null;

        // Invoke callbacks
        for (SnapshotJournal<?> journal : journalsToClose) {
            try {
                journal.close(this, result.wasAborted());
            } catch (Exception exception) {
                if (closeException == null) {
                    closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
                } else {
                    closeException.addSuppressed(exception);
                }
            }
        }

        journalsToClose.clear();

        if (manager.currentDepth == 0) {
            lifecycle = Lifecycle.ROOT_CLOSING;

            // Invoke root close callbacks
            for (SnapshotJournal<?> journal : manager.journalsToCommitWithRoot) {
                try {
                    journal.commit();
                } catch (Exception exception) {
                    if (closeException == null) {
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction root close callback.", exception);
                    } else {
                        closeException.addSuppressed(exception);
                    }
                }
            }

            manager.journalsToCommitWithRoot.clear();
        }

        // Only this check will allow openOuter operations.
        manager.currentDepth--;
        lifecycle = Lifecycle.NONE;

        // Throw exception if necessary
        if (closeException != null) {
            throw closeException;
        }
    }

    public enum Lifecycle {
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

        public boolean isOpen() {
            return this == OPEN;
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
