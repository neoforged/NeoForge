/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction.snapshots;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

/**
 * A snapshot only intended for the end result callbacks while not allocating new states for the snapshots or reversions.
 * The snapshots created are non-null and a singleton of {@link Singleton}
 * 
 * @see NotifyingSnapshotJournal
 */
public abstract class StatelessSnapshotJournal extends SnapshotJournal<StatelessSnapshotJournal.Singleton> {
    /**
     * Called on an unsuccessful transaction.
     */
    protected abstract void onRevert();

    /**
     * Called after the root transaction succeeded,
     * to perform irreversible actions such as {@code setChanged()} or neighbor updates.
     *
     * @throws IllegalStateException when trying to open a new transaction during this method as the current transaction is still in the process of closing.
     */
    protected abstract void onRootCommit();

    /**
     * The current state is ignored and instead a singleton instance is returned
     */
    @Override
    protected final Singleton createSnapshot() {
        return Singleton.INSTANCE;
    }

    @Override
    protected final void revertToSnapshot(Singleton ignored) {
        onRevert();
    }

    @Override
    protected final void onRootCommit(Singleton ignored) {
        onRootCommit();
    }

    /**
     * An instantiable singleton object where unlike void, can be null and have an accessible instance.
     */
    public static final class Singleton {
        private static final Singleton INSTANCE = new Singleton();

        private Singleton() {}
    }
}
