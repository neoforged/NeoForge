/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction.snapshots;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

/**
 * An abstracted out "Empty" snapshot that allows someone to ignore the snapshots themselves, but still able to respond to commits or other reference time points.
 */
public abstract class NotificationSnapshot extends SnapshotJournal<NotificationSnapshot.EmptyValue> {
    public static final NotificationSnapshot EMPTY = new NotificationSnapshot() {};

    public static final class EmptyValue {
        private static final EmptyValue INSTANCE = new EmptyValue();

        private EmptyValue() {}
    }

    @Override
    protected EmptyValue createSnapshot() {
        return EmptyValue.INSTANCE;
    }

    @Override
    protected void revertToSnapshot(EmptyValue snapshot) {
        //ignored
    }
}
