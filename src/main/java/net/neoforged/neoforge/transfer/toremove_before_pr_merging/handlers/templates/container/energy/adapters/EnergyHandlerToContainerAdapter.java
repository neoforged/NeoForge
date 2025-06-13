/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.adapters;

import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.IEnergyContainer;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
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
        if (wrappedHandler instanceof EnergyContainerToHandlerAdapter internal) {
            internal.set(index, value);
        }
        //todo assume we can't as transactions cannot be necessarly opened here
    }

    @Override
    public int getCapacity(int index) {
        return wrappedHandler.getCapacity(index);
    }

    @Override
    public SnapshotJournal<?> getSnapshotJournal(int index) {
        return NotificationSnapshot.EMPTY;
    }

    @Override
    public IEnergyHandler asHandler() {
        return IEnergyContainer.super.asHandler();
    }
}
