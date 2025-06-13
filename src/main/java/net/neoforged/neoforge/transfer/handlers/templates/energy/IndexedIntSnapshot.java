/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import java.util.ArrayList;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.NotificationSnapshot;

public class IndexedIntSnapshot extends SnapshotJournal<Integer> {
    @FunctionalInterface
    public interface Revert {
        void set(int index, int amount);
    }

    @FunctionalInterface
    public interface Snapshot {
        int get(int index);
    }

    private final Revert setter;
    private final Snapshot getter;
    private final int index;
    private final NotificationSnapshot setChangedSnapshot;

    public static IndexedIntSnapshot of(Revert setter, Snapshot getter, NotificationSnapshot snapshot) {
        return new IndexedIntSnapshot(setter, getter, 0, snapshot);
    }

    public static ArrayList<IndexedIntSnapshot> listOf(Revert setter, Snapshot getter, NotificationSnapshot notificationJournal, int size) {
        ArrayList<IndexedIntSnapshot> snapshots = new ArrayList<>();
        snapshots.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            snapshots.add(new IndexedIntSnapshot(setter, getter, i, notificationJournal));
        }
        return snapshots;
    }

    public IndexedIntSnapshot(Revert setter, Snapshot getter, int index, NotificationSnapshot snapshot) {
        this.index = index;
        this.setter = setter;
        this.getter = getter;
        this.setChangedSnapshot = snapshot;
    }

    @Override
    protected Integer createSnapshot() {
        return getter.get(index);
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        setter.set(index, snapshot);
    }

    @Override
    public void updateSnapshots(TransactionContext transaction) {
        setChangedSnapshot.updateSnapshots(transaction);
        super.updateSnapshots(transaction);
    }
}
