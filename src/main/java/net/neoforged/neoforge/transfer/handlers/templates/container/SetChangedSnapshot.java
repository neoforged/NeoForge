/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.container;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import org.jetbrains.annotations.Nullable;

// Boolean is used to prevent allocation. Null values are not allowed by SnapshotParticipant.
public final class SetChangedSnapshot extends SnapshotJournal<Boolean> {
    @Nullable
    private final Runnable callback;

    public static SetChangedSnapshot of(@Nullable Runnable callback) {
        return new SetChangedSnapshot(callback);
    }

    private SetChangedSnapshot(@Nullable Runnable callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean createSnapshot() {
        return Boolean.TRUE;
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {
        //ignored
    }

    @Override
    protected void onCommit(Boolean originalState) {
        runCallback();
    }

    public void runCallback() {
        if (callback != null)
            callback.run();
    }
}
