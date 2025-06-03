/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Objects;
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
 * <li>You may optionally override {@link #onCommit}: it is called at the of a transaction that modified the state.
 * For example, it could contain a call to {@code setChanged()}.</li>
 * <li>(Advanced!) You may optionally override {@link #releaseSnapshot}: it is called once a snapshot object will not be used,
 * for example you may wish to pool expensive state objects.</li>
 * </ul>
 *
 * <h3>More technical explanation</h3>
 *
 * <p>{@link #updateSnapshots} should be called before any modification.
 * This will save the state of this participant using {@link #createSnapshot} if no state was already saved for that transaction.
 * When the transaction is aborted and changes need to be rolled back, {@link #revertToSnapshot} will be called
 * to signal that the current state should revert to that of the snapshot.
 * The snapshot object is then {@linkplain #releaseSnapshot released}, and can be cached for subsequent use, or discarded.
 *
 * <p>When an outer transaction is committed, {@link #revertToSnapshot} will not be called so that the current state of this participant
 * is retained. {@link #onCommit} will be called after the transaction is closed
 * and then {@link #releaseSnapshot} will be called because the snapshot is not necessary anymore.
 *
 * @param <T> The objects that this participant uses to save its state snapshots.
 */
public abstract class SnapshotJournal<T> implements Transaction.CloseCallback, TransactionContext.RootCloseCallback {
    private final Int2ObjectMap<T> snapshots = new Int2ObjectLinkedOpenHashMap<>();
    @Nullable
    private T originalState = null;

    /**
     * Return a new <b>nonnull</b> object containing the current state of this participant.
     * <b>{@code null} may not be returned, or an exception will be thrown!</b>
     */
    protected abstract T createSnapshot();

    /**
     * Roll back to a state previously created by {@link #createSnapshot}.
     */
    protected abstract void revertToSnapshot(T snapshot);

    /**
     * Signals that the snapshot will not be used anymore, and is safe to cache for next calls to {@link #createSnapshot},
     * or discard entirely.
     */
    protected void releaseSnapshot(T snapshot) {}

    /**
     * Called after an outer transaction succeeded,
     * to perform irreversible actions such as {@code setChanged()} or neighbor updates.
     *
     * @param originalState state of this participant before the transactional operation.
     *                      This corresponds to the first {@link #createSnapshot() snapshot} that was created in the transactional operation.
     */
    protected void onCommit(T originalState) {}

    /**
     * Update the stored snapshots so that the changes happening as part of the passed transaction can be correctly
     * committed or rolled back.
     * This function should be called every time the participant is about to change its internal state as part of a transaction.
     */
    public void updateSnapshots(TransactionContext transaction) {
        if (snapshots.get(transaction.nestingDepth()) == null) {
            T snapshot = createSnapshot();
            Objects.requireNonNull(snapshot, "Snapshot may not be null!");

            snapshots.put(transaction.nestingDepth(), snapshot);
            transaction.addCloseCallback(this);
        }
    }

    @Override
    public void onClose(TransactionContext transaction, Transaction.Result result) {
        // Get and remove the relevant snapshot.
        T snapshot = snapshots.remove(transaction.nestingDepth());

        if (result.wasAborted()) {
            // If the transaction was aborted, we just revert to the state of the snapshot.
            revertToSnapshot(snapshot);
            releaseSnapshot(snapshot);
            if (transaction.nestingDepth() <= 0)
                snapshots.clear();
            return;
        }

        if (transaction.nestingDepth() <= 0) {
            originalState = snapshot;
            transaction.addRootCloseCallback(this);
            return;
        }

        if (snapshots.get(transaction.nestingDepth() - 1) == null) {
            // No snapshot yet, so move the snapshot one nesting level up.
            snapshots.put(transaction.nestingDepth() - 1, snapshot);
            // This is the first snapshot at this level: we need to call addCloseCallback.
            transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
        } else {
            // There is already an older snapshot at the nesting level above, just release the newer one.
            releaseSnapshot(snapshot);
        }
    }

    @Override
    public void afterRootClose(Transaction.Result result) {
        // The result is guaranteed to be COMMITTED,
        // as this is only scheduled during onClose() when the outer transaction is successful.
        // For the same reason, the originalState is known to be non-null.
        Objects.requireNonNull(originalState);

        onCommit(originalState);
        releaseSnapshot(originalState);
        originalState = null;
        //TODO should we clear the snapshots?
    }
}
