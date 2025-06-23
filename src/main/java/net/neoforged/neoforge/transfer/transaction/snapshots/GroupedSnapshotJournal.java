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
 */
public final class GroupedSnapshotJournal extends SnapshotJournal<GroupedSnapshotJournal.EmptyValue> {
    public static final SnapshotJournal<?> EMPTY = GroupedSnapshotJournal.of(null, null);

    @Nullable
    private final Runnable callback;
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

    public static GroupedSnapshotJournal commitWith(@Nullable Runnable commitCallback) {
        return new GroupedSnapshotJournal(commitCallback, null);
    }

    public static GroupedSnapshotJournal revertWith(@Nullable Runnable revertCallback) {
        return new GroupedSnapshotJournal(null, revertCallback);
    }

    @Override
    public void updateSnapshots(TransactionContext transaction) {
        super.updateSnapshots(transaction);
    }

    private GroupedSnapshotJournal(@Nullable Runnable commitCallback, @Nullable Runnable revertCallback) {
        this.callback = commitCallback;
        this.revertCallback = revertCallback;
    }

    @Override
    protected GroupedSnapshotJournal.EmptyValue createSnapshot() {
        return GroupedSnapshotJournal.EmptyValue.INSTANCE;
    }

    @Override
    protected void revertToSnapshot(EmptyValue snapshot) {
        if (revertCallback != null) {
            revertCallback.run();
        }
    }

    @Override
    protected void onCommit(EmptyValue originalState) {
        runCallback();
    }

    /**
     * A way to force running the callback if desired, instead of caching it elsewhere as well.
     */
    public void runCallback() {
        if (callback != null)
            callback.run();
    }

    public static final class EmptyValue {
        private static final EmptyValue INSTANCE = new EmptyValue();

        private EmptyValue() {}
    }
}
