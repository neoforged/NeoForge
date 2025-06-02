package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A global operation where participants guarantee atomicity: either the whole operation succeeds,
 * or it is completely aborted and rolled back.
 *
 * <p>One can imagine that transactions are like video game checkpoints.
 * <ul>
 * <li>{@linkplain #open Opening a transaction} with a try-with-resources block creates a checkpoint.</li>
 * <li>Modifications to game state can then happen.</li>
 * <li>Calling {@link #commit} validates the modifications that happened during the transaction,
 * essentially discarding the checkpoint.</li>
 * <li>Calling {@link #close} or doing nothing and letting the transaction be {@linkplain #close closed} at the end
 * of the try-with-resources block cancels any modification that happened during the transaction,
 * reverting to the checkpoint.</li>
 * <li>Calling {@link #open} with a non-{@code null} parent creates a new nested transaction, i.e. a new checkpoint with the current state.
 * Committing a nested transaction will validate the changes that happened, but they may
 * still be cancelled later if a parent transaction is cancelled.
 * Aborting a nested transaction immediately reverts the changes - cancelling any modification made after the call
 * to {@link #open}.</li>
 * </ul>
 *
 * <p>This is illustrated in the following example.
 *
 * <pre>{@code
 * try (Transaction outerTransaction = Transaction.open(null)) {
 *     // (A) some transaction operations
 *     try (Transaction nestedTransaction = outerTransaction.open(outerTransaction)) {
 *         // (B) more operations
 *         nestedTransaction.commit();
 *         // Commit the changes that happened in this transaction.
 *         // This is a nested transaction, so changes will only be applied if the outer
 *         // transaction is committed too.
 *     }
 *     // (C) even more operations
 *     outerTransaction.commit();
 *     // This is an outer transaction: changes (A), (B) and (C) are applied.
 * }
 * // If we hadn't committed the outerTransaction, all changes (A), (B) and (C) would have been reverted.
 * }</pre>
 *
 * <p>Participants are responsible for upholding this contract themselves, by using {@link #addCloseCallback}
 * to react to transaction close events and properly validate or revert changes.
 * Any action that modifies state outside of the transaction, such as calls to {@code markDirty()} or neighbor updates,
 * should be deferred until {@linkplain #addOuterCloseCallback after the outer transaction is closed}
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
@ApiStatus.NonExtendable
public interface Transaction extends AutoCloseable, TransactionContext {
    /**
     * Open a new outer transaction.
     *
     * @param parent the parent transaction, or null if this is the outermost transaction
     * @throws IllegalStateException If no parent is passed, but a transaction is already active on the current thread.
     * @throws IllegalStateException If a parent is passed, but it's not the current transaction.
     * @throws IllegalStateException If a parent is passed, but it was already closed.
     */
    static Transaction open(@Nullable TransactionContext parent) {
        return TransactionManagerImpl.MANAGERS.get().open(parent);
    }

    /**
     * @return True if a transaction is open or closing on the current thread, and false otherwise.
     */
    static boolean isOpen() {
        return getLifecycle() != Lifecycle.NONE;
    }

    /**
     * @return The current lifecycle of the transaction stack on this thread.
     */
    static Lifecycle getLifecycle() {
        return TransactionManagerImpl.MANAGERS.get().getLifecycle();
    }

//    /**
//     * Retrieve the currently open transaction, or null if there is none.
//     *
//     * <p><b>Usage of this function is strongly discouraged</b>, this is why it is deprecated and contains {@code unsafe} in its name.
//     * The transaction may be aborted unbeknownst to you and anything you think that you have committed might be undone.
//     * Only use it if you have no way to pass the transaction down the stack, for example if you are implementing compat with a simulation-based API,
//     * and you know what you are doing, for example because you opened the outer transaction.
//     *
//     * @throws IllegalStateException If called from a close or outer close callback.
//     * @deprecated Only use if you absolutely need it, there is almost always a better way.
//     */
//    @ApiStatus.Internal
//    @Nullable
//    static TransactionContext getCurrentUnsafe() {
//        return TransactionManagerImpl.MANAGERS.get().getCurrentUnsafe();
//    }


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
    void commit();

    /**
     * Abort the current transaction if it was not closed already.
     */
    @Override
    void close();

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
         * The current transaction is invoking its outer close callbacks.
         */
        OUTER_CLOSING
    }
}
