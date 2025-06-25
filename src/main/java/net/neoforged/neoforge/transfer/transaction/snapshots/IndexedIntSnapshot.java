/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction.snapshots;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * A snapshot journal that can keep track of {@code int} values in an indexable structure. Also takes in a SnapshotJournal
 * that can be updated when an index gets written to. An example is using a {@link GroupedSnapshotJournal} to handle
 * the scenario of doing a single commit for something like {@link BlockEntity#setChanged()}.
 */
public class IndexedIntSnapshot extends SnapshotJournal<Integer> {
    /**
     * Apply the value for snapshotting at the specified index. This value should be the last valid value from the {@link Snapshot}
     * during the transaction chain.
     */
    @FunctionalInterface
    public interface Revert {
        void set(int index, int value);
    }

    /**
     * Gets the current value for snapshotting. Given some index, what would the value be if we were to need to revert.
     */
    @FunctionalInterface
    public interface Snapshot {
        Integer get(int index);
    }

    private final int index;
    private final Revert setter;
    private final Snapshot getter;
    //One shared setChanged journal shared across all indices
    @Nullable
    private final SnapshotJournal<?> onChangedSnapshot;

    /**
     * Returns a snapshot handling a specific index of a container for integers.
     * 
     * @param setter   The reversion method to apply the snapshot value should a transaction fail.
     * @param getter   The snapshot method to take note of the current value to store as a snapshot in the {@link SnapshotJournal}
     * @param onChange A groupable journal that is updated along side each index. This is typically a shared reference between indices,
     *                 so that only one {@link SnapshotJournal#onCommit} or {@link SnapshotJournal#revertToSnapshot} can be applied in
     *                 scenarios where it may be doing an expensive call; rather than also doing this once per index change.
     * @return {@link SnapshotJournal} for handling integer value snapshotting at an index
     */
    public static IndexedIntSnapshot of(Revert setter, Snapshot getter, @Nullable SnapshotJournal<?> onChange) {
        return new IndexedIntSnapshot(0, setter, getter, onChange);
    }

    /**
     * Returns a snapshot handling a specific index of a container for integers.
     * 
     * @param size     The number of indices to make
     * @param setter   The reversion method to apply the snapshot value should a transaction fail.
     * @param getter   The snapshot method to take note of the current value to store as a snapshot in the {@link SnapshotJournal}
     * @param onChange A groupable journal that is updated along side each index. This is typically a shared reference between indices,
     *                 so that only one {@link SnapshotJournal#onCommit} or {@link SnapshotJournal#revertToSnapshot} can be applied in
     *                 scenarios where it may be doing an expensive call; rather than also doing this once per index change.
     * @return A list of {@link SnapshotJournal SnapshotJournals} for handling integer value snapshotting at their respective indices
     */
    public static List<IndexedIntSnapshot> listOf(int size, Revert setter, Snapshot getter, SnapshotJournal<?> onChange) {
        List<IndexedIntSnapshot> snapshots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            snapshots.add(new IndexedIntSnapshot(i, setter, getter, onChange));
        }
        return snapshots;
    }

    private IndexedIntSnapshot(int index, Revert setter, Snapshot getter, @Nullable SnapshotJournal<?> onChange) {
        this.index = index;
        this.setter = setter;
        this.getter = getter;
        this.onChangedSnapshot = onChange;
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
        if (onChangedSnapshot != null)
            onChangedSnapshot.updateSnapshots(transaction);
        super.updateSnapshots(transaction);
    }
}
