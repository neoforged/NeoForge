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
     * Called on a successful transaction.
     */
    protected abstract void onCommit();

    /**
     * The current state is ignored and instead a singleton instance is returned
     */
    @Override
    protected final Singleton createSnapshot() {
        return Singleton.INSTANCE;
    }

    /**
     * Notifies the journal that the transaction requested to revert
     */
    @Override
    protected final void revertToSnapshot(Singleton ignored) {
        onRevert();
    }

    /**
     * Notifies the journal the transaction was successful and should commit
     */
    @Override
    protected final void onCommit(Singleton ignored) {
        onCommit();
    }

    /**
     * An instantiable singleton object where unlike void, can be null and have an accessible instance.
     */
    public static final class Singleton {
        private static final Singleton INSTANCE = new Singleton();

        private Singleton() {}
    }
}
