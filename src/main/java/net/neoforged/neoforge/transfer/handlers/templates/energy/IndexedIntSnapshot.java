/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import java.util.ArrayList;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandlerModifiable;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.NotificationSnapshot;
import net.neoforged.neoforge.transfer.transaction.snapshots.SetChangedSnapshot;

public class IndexedIntSnapshot extends SnapshotJournal<Integer> {
    private final IEnergyHandlerModifiable handler;
    private final int index;
    private final NotificationSnapshot setChangedSnapshot;

    public IndexedIntSnapshot(IEnergyHandlerModifiable handler, int index, NotificationSnapshot snapshot) {
        this.handler = handler;
        this.index = index;
        this.setChangedSnapshot = snapshot;
    }

    @Override
    protected Integer createSnapshot() {
        return handler.getAmount(index);
    }

    @Override
    protected void revertToSnapshot(Integer snapshot) {
        handler.set(index, snapshot);
    }

    @Override
    public void updateSnapshots(TransactionContext transaction) {
        setChangedSnapshot.updateSnapshots(transaction);
        super.updateSnapshots(transaction);
    }

    public static void initSnapshots(IEnergyHandlerModifiable handler, ArrayList<IndexedIntSnapshot> snapshots, int size, SetChangedSnapshot setChangedSnapshot) {
        snapshots.ensureCapacity(size);
        for (var i = 0; i < size; i++) {
            snapshots.add(new IndexedIntSnapshot(handler, i, setChangedSnapshot));
        }
    }
}
