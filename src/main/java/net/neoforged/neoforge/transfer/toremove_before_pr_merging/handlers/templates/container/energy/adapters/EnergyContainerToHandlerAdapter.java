/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.adapters;

import java.util.Objects;
import net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferAttachment;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.energy.IEnergyHandlerModifiable;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.IEnergyContainer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public record EnergyContainerToHandlerAdapter(
        IEnergyContainer container,
        IHandleIOBehaviour behavior) implements IEnergyHandlerModifiable {
    @Override
    public int size() {
        return container().size();
    }

    @Override
    public int getAmount(int index) {
        return container.get(index);
    }

    @Override
    public int getCapacity(int index) {
        return container.getCapacity(index);
    }

    @Override
    public boolean supportsInsertion(int index) {
        return behavior.canInsert(index);
    }

    @Override
    public boolean supportsExtraction(int index) {
        return behavior.canExtract(index);
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        amount = Math.min(container.getMaxInsertRate(), amount);
        if (amount <= 0) return 0;

        var handled = 0;
        var indices = size();
        for (var index = 0; index < indices; index++) {
            if (handled == amount) break;
            //We don't need to check if the index is valid in this case since we already know our index is within bounds
            handled += insertCommon(index, amount - handled, transaction);
        }
        return handled;
    }

    @Override
    public int insert(int index, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        amount = Math.min(container.getMaxInsertRate(), amount);
        if (amount <= 0) return 0;

        return insertCommon(index, amount, transaction);
    }

    /**
     * This was chosen to be separate from {@link EnergyBufferAttachment#insert(int, int, TransactionContext)} to provide both parity
     * with the IResourceHandler and allow more accurate index checks when doing the loop variant.
     * <p>
     * The added benefit is less double-checking in runtime on data we already know
     */
    private int insertCommon(int index, int amount, TransactionContext transaction) {
        if (!behavior.canInsert(index)) return 0;

        var currentAmount = container.get(index);
        var capacity = getCapacity(index);

        int inserted, newAmount;
        inserted = Math.min(capacity - currentAmount, amount);
        newAmount = currentAmount + inserted;

        if (newAmount > 0) {
            container.getSnapshotJournal(index).updateSnapshots(transaction);
            set(index, newAmount);
        }
        return inserted;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        amount = Math.min(container.getMaxExtractRate(), amount);
        if (amount <= 0) return 0;

        var handled = 0;
        var indices = size();
        for (var index = 0; index < indices; index++) {
            if (handled == amount) break;
            //We don't need to check if the index is valid in this case since we already know our index is within bounds
            handled += indexedExtract(index, amount - handled, transaction);
        }
        return handled;
    }

    @Override
    public int extract(int index, int amount, TransactionContext transaction) {
        //This check is done per external index call
        Objects.checkIndex(index, size());
        amount = Math.min(container.getMaxExtractRate(), amount);
        if (amount <= 0) return 0;

        return indexedExtract(index, amount, transaction);
    }

    private int indexedExtract(int index, int amount, TransactionContext transaction) {
        if (!behavior.canExtract(index)) return 0;

        var currentAmount = container.get(index);
        int handledAmount = Math.min(amount, currentAmount);

        container.getSnapshotJournal(index).updateSnapshots(transaction);
        set(index, currentAmount - handledAmount);
        return handledAmount;
    }

    @Override
    public void set(int index, int amount) {
        container.set(index, amount);
    }
}
