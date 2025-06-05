/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction.snapshots;

import org.jetbrains.annotations.Nullable;

/**
 * A notification snapshot with a runnable callback. Useful for when you are responding from a commit, and you want to set a change once.
 */
public final class SetChangedSnapshot extends NotificationSnapshot {
    @Nullable
    private final Runnable callback;

    public static SetChangedSnapshot of(@Nullable Runnable callback) {
        return new SetChangedSnapshot(callback);
    }

    private SetChangedSnapshot(@Nullable Runnable callback) {
        this.callback = callback;
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
}
