/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * A journal that modifies itself during transactions,
 * saving snapshots of its state in objects of type {@code T} in case it needs to revert to a previous state.
 *
 * <h3>How to use from subclasses</h3>
 * <ul>
 * <li>Call {@link #updateSnapshots} right before the state of your subclass is modified in a transaction.</li>
 * <li>Override {@link #createSnapshot}: it is called when necessary to create an object representing the state of your subclass.</li>
 * <li>Override {@link #revertToSnapshot}: it is called when necessary to revert to a previous state of your subclass.</li>
 * <li>You may optionally override {@link #onRootCommit}: it is called at the end of a transaction that modified the state.
 * For example, it could contain a call to {@code setChanged()}.</li>
 * <li>(Advanced!) You may optionally override {@link #releaseSnapshot}: it is called once a snapshot object will not be used,
 * for example you may wish to pool expensive state objects.</li>
 * </ul>
 *
 * <h3>More technical explanation</h3>
 *
 * <p>{@link #updateSnapshots} should be called before any modification.
 * This will record the state in the journal using {@link #createSnapshot} if no state was already saved for that transaction.
 * When the transaction is aborted and changes need to be rolled back, {@link #revertToSnapshot} will be called
 * to signal that the current state should revert to that of the snapshot.
 * The snapshot object is then {@linkplain #releaseSnapshot released}, and can be cached for subsequent use, or discarded.
 *
 * <p>When the root transaction is committed, {@link #revertToSnapshot} will not be called so that the current state of the journal
 * is retained. {@link #onRootCommit} will be called after the transaction is closed
 * and then {@link #releaseSnapshot} will be called because the snapshot is not necessary anymore.
 *
 * @param <T> The objects that this journal uses to record its state snapshots.
 */
public abstract class SnapshotJournal<T extends @Nullable Object> {
    /**
     * Used for entries of {@link #snapshots} that do not correspond to a snapshot.
     * {@code null} corresponds to a snapshot that happens to be {@code null}.
     */
    private static final Object NO_SNAPSHOT = new Object();

    private final ArrayList<T> snapshots = new ArrayList<>();

    @Nullable
    private T originalState = null;

    /**
     * Return a new <b>nonnull</b> object containing the current state of this journal.
     * <b>{@code null} may not be returned, or an exception will be thrown!</b>
     */
    protected abstract T createSnapshot();

    /**
     * Roll back to a state previously created by {@link #createSnapshot}.
     */
    protected abstract void revertToSnapshot(T snapshot);

    /**
     * Signals that the snapshot will not be used anymore, and is safe to cache for future calls to {@link #createSnapshot},
     * or discard entirely.
     */
    protected void releaseSnapshot(T snapshot) {}

    /**
     * Called after the root transaction was successfully committed,
     * to perform irreversible actions such as {@code setChanged()} or neighbor updates.
     *
     * <p>When a root transaction is being closed,
     * all journals for which {@code onRootCommit} will be called are stored in a global thread-local queue.
     * The processing of this queue starts <strong>immediately after</strong> the root transaction is closed.
     * As such, new root transactions can safely be opened from this method.
     *
     * <p>When a root transaction is opened from {@code onRootCommit},
     * any journal might be modified, leading to more {@code onRootCommit} callbacks being enqueued:
     * <ul>
     * <li>A journal that is already enqueued for {@code onRootCommit} will not be enqueued a second time.
     * It will thus be notified a single time for changes that spanned multiple transactions.
     * The {@code originalState} will be the state at the beginning of the first of these transactions.</li>
     * <li>A journal whose {@code onRootCommit} was already processed will be enqueued again.
     * The journal will be notified a second time, with {@code originalState} the state at the beginning of the <strong>second</strong> transaction.</li>
     * <li>In particular, a journal is removed from the queue immediately before {@code onRootCommit} is called.
     * Should the journal be modified again from its own {@code onRootCommit}, it will be added to the queue,
     * and {@code onRootCommit} will be called again later.</li>
     * </ul>
     *
     * <p>Given the large amount of actions that can happen between the last modification and the call to {@code onRootCommit},
     * journals should not depend on {@code onRootCommit} being called immediately for correctness,
     * and implementations of this method should be careful (e.g. in case the journal got removed from the level).
     * For example, skipping block change notifications because the block was removed from the level
     * is preferable than crashing or silently overwriting the block.
     *
     * @param originalState state of this journal before the transactional operations.
     *                      This corresponds to the first {@link #createSnapshot() snapshot} that was created in the transactional operations.
     */
    protected void onRootCommit(T originalState) {}

    /**
     * Update the stored snapshots so that the changes happening as part of the passed transaction can be correctly
     * committed or rolled back.
     * This function should be called every time the journal is about to change its internal state as part of a transaction.
     * However, only the first snapshot taken of that depth will be taken.
     */
    @SuppressWarnings("unchecked")
    public void updateSnapshots(TransactionContext transaction) {
        int currentDepth = transaction.depth();

        snapshots.ensureCapacity(currentDepth);
        for (int i = snapshots.size(); i <= currentDepth; i++) {
            snapshots.add((T) NO_SNAPSHOT);
        }

        if (snapshots.get(currentDepth) == NO_SNAPSHOT) {
            snapshots.set(currentDepth, createSnapshot());

            // This is a special case where we need to cast to access internal Transaction methods.
            // You should never, however, cast to call commit or close!
            var transactionImpl = (Transaction) transaction;
            transactionImpl.validateOpen();
            transactionImpl.journalsToClose.add(this);
        }
    }

    /**
     * Perform the required state management when a transaction is closed.
     */
    void onClose(Transaction transaction, boolean wasAborted) {
        int currentDepth = transaction.depth();

        // Get and remove the relevant snapshot.
        T snapshot = snapshots.remove(currentDepth);

        if (wasAborted) {
            // If the transaction was aborted, we just revert to the state of the snapshot.
            revertToSnapshot(snapshot);
            releaseSnapshot(snapshot);
        } else if (currentDepth <= 0) {
            // The transaction is the root.
            if (originalState == null) {
                originalState = snapshot;
                transaction.manager.rootCommitQueue.add(this);
            } else {
                // If originalState was not null, it means that an onRootCommit callback is already scheduled.
                // This means that this journal got modified in a transaction opened from some onRootCommit callback.
                // In this case we just wait for the already-registered callback to run.
                releaseSnapshot(snapshot);
            }
        } else if (snapshots.get(currentDepth - 1) == NO_SNAPSHOT) {
            // No snapshot yet, so move the snapshot one depth up.
            snapshots.set(currentDepth - 1, snapshot);
            // This is the first snapshot at this level: we need to add the closing journal to the previous depth.
            transaction.manager.getOpenTransaction(currentDepth - 1).journalsToClose.add(this);
        } else {
            // There is already an older snapshot at the depth above, just release the newer one.
            releaseSnapshot(snapshot);
        }
    }

    void callOnRootCommit() {
        // This is only scheduled during onClose() when the root transaction is successful,
        // hence the originalState is known to correspond to the first snapshot even if nullable.
        T originalState = this.originalState;
        // Clear this.originalState immediately rather than later, because onRootCommit might trigger new transactions,
        // which might write a new value to this.originalState and schedule this journal for a root commit again.
        this.originalState = null;
        onRootCommit(originalState);
        releaseSnapshot(originalState);
    }
}
