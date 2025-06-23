/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;

/**
 * A global operation where participants guarantee atomicity: either the whole operation succeeds,
 * or it is completely aborted and rolled back.
 *
 * <p>One can imagine that transactions are like video game checkpoints.
 * <ul>
 * <li>{@linkplain TransactionManager#open Opening a transaction} with a try-with-resources block creates a checkpoint.</li>
 * <li>Modifications to game state can then happen.</li>
 * <li>Calling {@link #commit} validates the modifications that happened during the transaction,
 * essentially discarding the checkpoint.</li>
 * <li>Calling {@link #close} or doing nothing and letting the transaction be {@linkplain #close closed} at the end
 * of the try-with-resources block cancels any modification that happened during the transaction,
 * reverting to the checkpoint.</li>
 * <li>Calling {@link TransactionManager#open} with a non-{@code null} parent creates a new nested transaction, i.e. a new checkpoint with the current state.
 * Committing a nested transaction will validate the changes that happened, but they may
 * still be cancelled later if a parent transaction is cancelled.
 * Aborting a nested transaction immediately reverts the changes - cancelling any modification made after the call
 * to {@link TransactionManager#open}.</li>
 * </ul>
 *
 * <p>This is illustrated in the following example.
 *
 * <pre>{@code
 * try (Transaction outerTransaction = TransactionManager.open(null)) {
 *     // (A) some transaction operations
 *     try (Transaction nestedTransaction = TransactionManager.open(outerTransaction)) {
 *         // (B) more operations
 *         nestedTransaction.commit();
 *         // Commit the changes that happened in this transaction.
 *         // This is a nested transaction, so changes will only be applied if the outer
 *         // transaction is committed too.
 *         // auto-close the transaction when exiting the try block
 *     }
 *     // (C) even more operations
 *     outerTransaction.commit();
 *     // This is an outer transaction: changes (A), (B) and (C) are applied.
 *     // auto-close the transaction when exiting the try block
 * }
 * // If we hadn't committed the outerTransaction, all changes (A), (B) and (C) would have been reverted.
 * }</pre>
 *
 * <p>Participants are responsible for upholding this contract themselves, by using {@link #addCloseCallback}
 * to react to transaction close events and properly validate or revert changes.
 * Any action that modifies state outside the transaction, such as calls to {@code markDirty()} or neighbor updates,
 * should be deferred until {@linkplain #addRootCloseCallback after the outer transaction is closed}
 * to give every participant a chance to react to transaction close events.
 *
 * <p>This is very low-level for most applications, and most participants should subclass {@link SnapshotJournal}
 * that will take care of properly maintaining their state.
 *
 * <p>Participants should generally be passed a {@link TransactionContext} parameter instead of the full {@code Transaction},
 * to make sure they don't call {@link #commit} or {@link #close} mistakenly.
 *
 * <p>Every transaction is only valid on the thread it was opened on,
 * and attempts to use it on another thread will throw an exception.
 * Consequently, transactions can be concurrent across multiple threads, as long as they don't share any state.
 */
public final class Transaction implements AutoCloseable, TransactionContext {
    /**
     * Close the current transaction, committing all the changes that happened during this transaction and its <b>committed</b> children transactions.
     * If this transaction was opened with a {@code null} parent, all changes are applied.
     * If this transaction was opened with a non-{@code null} parent, all changes will be applied when and if the changes of
     * the parent transactions are applied.
     * <p>
     * This would be familiar to using `execute` in the old simulated handlers
     *
     * @throws IllegalStateException If this function is not called on the thread this transaction was opened in.
     * @throws IllegalStateException If this transaction is not the current transaction.
     * @throws IllegalStateException If this transaction was closed.
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
    public int nestingDepth() {
        validateCurrentThread();
        return nestingDepth;
    }

    @Override
    public Transaction getOpenTransaction(int nestingDepth) {
        validateCurrentThread();

        if (nestingDepth < 0) {
            throw new IndexOutOfBoundsException("Nesting depth may not be negative.");
        }

        if (nestingDepth > manager.currentDepth) {
            throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + nestingDepth);
        }

        Transaction transaction = manager.stack.get(nestingDepth);
        transaction.validateOpen();
        return transaction;
    }

    @Override
    public void addCloseCallback(CloseCallback closeCallback) {
        validateCurrentThread();
        validateOpen();
        closeCallbacks.add(closeCallback);
    }

    @Override
    public void addRootCloseCallback(RootCloseCallback rootCloseCallback) {
        validateCurrentThread();
        // Note: we don't call validateOpen() because this transaction may not be open if this is called during a CloseCallback.
        // We rely on a currentDepth check instead, as the depth is only set to -1 at the very end of close(Result).

        if (manager.currentDepth == -1) {
            throw new IllegalStateException("There is no open transaction on this thread.");
        }

        manager.rootCloseCallbacks.add(rootCloseCallback);
    }

    @Override
    public String toString() {
        return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(nestingDepth, lifecycle.name(), manager.thread.getName());
    }

    //Internal handling
    Lifecycle lifecycle = Lifecycle.NONE;

    private final TransactionManager manager;
    private final int nestingDepth;
    private final List<CloseCallback> closeCallbacks = new ArrayList<>();

    //Package protected constructor
    Transaction(TransactionManager manager, int nestingDepth) {
        this.manager = manager;
        this.nestingDepth = nestingDepth;
    }

    private void validateCurrentThread() {
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

        if (manager.currentDepth != -1) {
            if (manager.stack.get(manager.currentDepth) == this) return;
        }

        //TODO validate this is handling the use case of showing a dev which transactions are being opened / closed (on top of the stacktrace)
        var self = TransactionManager.debugNameFrom(manager.debugMap.get(nestingDepth));
        var actual = TransactionManager.debugNameFrom(manager.debugMap.get(manager.currentDepth));

        CrashReport report = CrashReport.forThrowable(new IllegalStateException("Transaction function was called on a transaction with depth %d, but the current transaction has depth %d."
                .formatted(nestingDepth, manager.currentDepth)), "Transacting");
        report.addCategory("Transaction Opening")
                .setDetail("Existing Transaction", actual)
                .setDetail("Current Depth", manager.currentDepth)
                .setDetail("Attempted Transaction", self)
                .setDetail("Nesting Depth", nestingDepth);

        throw new ReportedException(report);
    }

    // Validate that this transaction is open.
    void validateOpen() {
        if (lifecycle != Lifecycle.OPEN) {
            throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction.");
        }
    }

    void close(Result result) {
        validateCurrentTransaction();
        validateOpen();
        // Block transaction operations
        lifecycle = Lifecycle.CLOSING;

        // Note: it is important that we don't let exceptions corrupt the global state of the transaction manager.
        // That is why any callback has to run inside a try block.
        RuntimeException closeException = null;

        // Invoke callbacks in reverse order
        for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
            try {
                closeCallbacks.get(i).onClose(this, result);
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

            // Invoke outer close callbacks in reverse order
            for (int i = manager.rootCloseCallbacks.size() - 1; i >= 0; i--) {
                try {
                    manager.rootCloseCallbacks.get(i).afterRootClose(result);
                } catch (Exception exception) {
                    if (closeException == null) {
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction root close callback.", exception);
                    } else {
                        closeException.addSuppressed(exception);
                    }
                }
            }

            manager.rootCloseCallbacks.clear();
        }

        // Only this check will allow openOuter operations.
        manager.currentDepth--;
        lifecycle = Lifecycle.NONE;

        // Throw exception if necessary
        if (closeException != null) {
            throw closeException;
        }
    }
}
