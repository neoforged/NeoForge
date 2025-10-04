/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import org.jetbrains.annotations.Nullable;

/**
 * A journal that can be added to a transaction with {@link #updateSnapshots(TransactionContext)},
 * and will invoke a callback {@linkplain #rootCommitCallback if/when the root transaction is committed}.
 *
 * <p>This journal does not save any state snapshot by itself,
 * so it should be used in conjunction with other {@linkplain SnapshotJournal journals} that handle the actual state.
 */
public final class RootCommitJournal extends SnapshotJournal<@Nullable Void> {
    private final Runnable rootCommitCallback;

    public RootCommitJournal(Runnable rootCommitCallback) {
        this.rootCommitCallback = rootCommitCallback;
    }

    @Override
    protected @Nullable Void createSnapshot() {
        return null;
    }

    @Override
    protected void revertToSnapshot(@Nullable Void snapshot) {}

    @Override
    protected void onRootCommit(@Nullable Void originalState) {
        rootCommitCallback.run();
    }
}
