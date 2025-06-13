/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public final class HopperJournal extends SnapshotJournal<Boolean> {
    private final Revert setter;
    private final Snapshot getter;

    public HopperJournal(Revert setter, Snapshot getter) {
        this.setter = setter;
        this.getter = getter;
    }

    @FunctionalInterface
    public interface Revert {
        void set(boolean value);
    }

    @FunctionalInterface
    public interface Snapshot {
        boolean get();
    }

    @Override
    protected Boolean createSnapshot() {
        return getter.get();
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {
        setter.set(snapshot);
    }
}
