/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.adapters;

import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandlerModifiable;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.IEnergyContainer;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.snapshots.NotificationSnapshot;

/**
 * Adapts any arbitrary resource handlers and wraps it into a IResourceContainer. Note, this may have odd behaviour when dealing with other mod's handlers here.
 */
public record EnergyHandlerToContainerAdapter(
        IEnergyHandler wrappedHandler) implements IEnergyContainer {
    @Override
    public int size() {
        return wrappedHandler.size();
    }

    @Override
    public int getMaxInsertRate() {
        //This rate is lost in the wrap given it isn't something provided by the handler
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxExtractRate() {
        //This rate is lost in the wrap given it isn't something provided by the handler
        return Integer.MAX_VALUE;
    }

    @Override
    public int get(int index) {
        return wrappedHandler.getAmount(index);
    }

    @Override
    public void set(int index, int value) {
        if (wrappedHandler instanceof IEnergyHandlerModifiable modifiable) {
            modifiable.set(index, value);
            return;
        }

        try (var transaction = Transaction.open(Transaction.ROOT)) {
            wrappedHandler.extract(index, Integer.MAX_VALUE, transaction);
            wrappedHandler.insert(index, value, transaction);
        }
    }

    @Override
    public int getCapacity(int index) {
        return wrappedHandler.getCapacity(index);
    }

    @Override
    public SnapshotJournal<?> getSnapshotJournal(int index) {
        return NotificationSnapshot.INSTANCE;
    }

    @Override
    public IEnergyHandlerModifiable asHandler() {
        return wrappedHandler instanceof IEnergyHandlerModifiable modifiable ? modifiable : IEnergyContainer.super.asHandler();
    }
}
