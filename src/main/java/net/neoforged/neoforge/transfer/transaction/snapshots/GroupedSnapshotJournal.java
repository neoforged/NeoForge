/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction.snapshots;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * A snapshot journal with a runnable callback that plays only on a successful commit or on reversion. This is intended to be used when
 * you may have multiple journals bound to individual indices, but want to act only once on failure/success at the end of the transaction chain.
 * <p>
 * A typical example is when you have an inventory of say 100 different indices, and each index has its own journal. This can be set to
 * update with them, (like {@link IndexedIntSnapshot} does in {@link IndexedIntSnapshot#updateSnapshots(TransactionContext)}) so that any time a new snapshot is taken,
 * this will update at the same time. Since we only store one snapshot per depth in a transaction, this will only have 1 regardless of which and how many indices were updated in that chain.
 * <p>
 * When the transaction is committed, {@link #commitCallback} will run; and when the transaction is reverted {@link #revertCallback} will instead. Only one per transaction chain will be called and only once.
 */
public final class GroupedSnapshotJournal extends SnapshotJournal<GroupedSnapshotJournal.IgnoredValue> {
    @Nullable
    private final Runnable commitCallback;
    @Nullable
    private final Runnable revertCallback;

    /**
     * Creates a grouped snapshot journal with custom commit and revert logic
     * in scenarios you don't need to allocate a new snapshot.
     * Only one runnable in a given transaction will run. It is either successful or not.
     *
     * @param commitCallback Action called when the transaction successfully commits its chain.
     * @param revertCallback Action called when the transaction reverts to a snapshot.
     * @return A Journal able to be take notes of when a value was changed, but doesn't allocate any value.
     */
    public static GroupedSnapshotJournal of(@Nullable Runnable commitCallback, @Nullable Runnable revertCallback) {
        return new GroupedSnapshotJournal(commitCallback, revertCallback);
    }

    /**
     * Creates a grouped snapshot journal with custom commit
     * in scenarios you don't need to allocate a new snapshot.
     * Only one runnable in a given transaction will run. It is either successful or not.
     *
     * @param commitCallback Action called when the transaction successfully commits its chain.
     * @return A Journal able to be take notes of when a value was changed, but doesn't allocate any value.
     */
    public static GroupedSnapshotJournal commitWith(@Nullable Runnable commitCallback) {
        return new GroupedSnapshotJournal(commitCallback, null);
    }

    public static GroupedSnapshotJournal revertWith(@Nullable Runnable revertCallback) {
        return new GroupedSnapshotJournal(null, revertCallback);
    }

    private GroupedSnapshotJournal(@Nullable Runnable commitCallback, @Nullable Runnable revertCallback) {
        this.commitCallback = commitCallback;
        this.revertCallback = revertCallback;
    }

    @Override
    protected IgnoredValue createSnapshot() {
        return IgnoredValue.INSTANCE;
    }

    @Override
    protected void revertToSnapshot(IgnoredValue snapshot) {
        if (revertCallback != null) {
            revertCallback.run();
        }
    }

    @Override
    protected void onCommit(IgnoredValue originalState) {
        runCommitCallback();
    }

    /**
     * A way to force running the callback if desired, instead of caching it elsewhere as well.
     */
    public void runCommitCallback() {
        if (commitCallback != null)
            commitCallback.run();
    }

    public static final class IgnoredValue {
        private static final IgnoredValue INSTANCE = new IgnoredValue();

        private IgnoredValue() {}
    }
}
