/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import java.util.ArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.GroupedSnapshotJournal;

/**
 * A snapshot journal that can keep track of {@code int} values in an indexable structure. Also takes in a SnapshotJournal
 * that can be updated when an index gets written to. An example is using a {@link GroupedSnapshotJournal} to handle
 * the scenario of doing a single commit for something like {@link BlockEntity#setChanged()}.
 * If no additional snapshot is needed, you can pass in {@link GroupedSnapshotJournal#EMPTY}
 */
public class IndexedIntSnapshot extends SnapshotJournal<Integer> {
    @FunctionalInterface
    public interface Revert {
        void set(int index, int amount);
    }

    @FunctionalInterface
    public interface Snapshot {
        Integer get(int index);
    }

    private final int index;
    private final Revert setter;
    private final Snapshot getter;
    //One shared setChanged journal shared across all indices
    private final SnapshotJournal<?> setChangedSnapshot;

    public static IndexedIntSnapshot of(Revert setter, Snapshot getter, SnapshotJournal<?> snapshot) {
        return new IndexedIntSnapshot(0, setter, getter, snapshot);
    }

    public static ArrayList<IndexedIntSnapshot> listOf(int size, Revert setter, Snapshot getter, SnapshotJournal<?> notificationJournal) {
        ArrayList<IndexedIntSnapshot> snapshots = new ArrayList<>();
        snapshots.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            snapshots.add(new IndexedIntSnapshot(i, setter, getter, notificationJournal));
        }
        return snapshots;
    }

    private IndexedIntSnapshot(int index, Revert setter, Snapshot getter, SnapshotJournal<?> snapshot) {
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
