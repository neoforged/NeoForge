/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.adapters;

import java.util.Objects;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.IEnergyContainer;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

/**
 * A slice of a {@link IEnergyContainer}. Changes to the slice should reflect in the parent.
 */
public record EnergyContainerSlice(
        IEnergyContainer parent,
        int start,
        int length) implements IEnergyContainer {
    @Override
    public int size() {
        return length;
    }

    @Override
    public int getMaxInsertRate() {
        return parent.getMaxInsertRate();
    }

    @Override
    public int getMaxExtractRate() {
        return parent.getMaxExtractRate();
    }

    @Override
    public int get(int index) {
        Objects.checkIndex(index, length);
        return parent.get(index + start);
    }

    @Override
    public void set(int index, int value) {
        Objects.checkIndex(index, length);
        parent.set(index + start, value);
    }

    @Override
    public SnapshotJournal<?> getSnapshotJournal(int index) {
        return parent.getSnapshotJournal(index + start);
    }

    @Override
    public int getCapacity(int index) {
        return parent.getCapacity(index + start);
    }

    @Override
    public IEnergyContainer slice(int from, int to) {
        Objects.checkFromToIndex(from, to, length);
        return new EnergyContainerSlice(parent, this.start + from, to - from);
    }
}
