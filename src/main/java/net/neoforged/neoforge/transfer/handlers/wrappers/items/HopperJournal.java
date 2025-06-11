/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public abstract class HopperJournal extends SnapshotJournal<Boolean> {
    public abstract void set(boolean value);

    public abstract boolean get();

    @Override
    protected Boolean createSnapshot() {
        return get();
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {
        set(snapshot);
    }
}
