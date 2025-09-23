/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Extension methods for {@link Container}. These methods are used by {@link VanillaContainerWrapper},
 * to allow containers to be integrated in a transaction.
 */
public interface ContainerExtension {
    private Container self() {
        return (Container) this;
    }

    /**
     * An extension of {@link Container#setItem(int, ItemStack)} that allows non-transactional side-effects to be skipped.
     * Non-transactional side-effects include for example calling {@code setChanged} or making changes to the world.
     *
     * @param insideTransaction When {@code true}, non-transactional actions should be deferred.
     *                          When {@code false}, non-transactional actions can be performed immediately.
     * @see #onTransfer(int,int,TransactionContext) Overriding onTransfer, to react to transactions and defer non-transactional side-effects until the transaction is committed.
     */
    @ApiStatus.OverrideOnly
    default void setItem(int slot, ItemStack stack, boolean insideTransaction) {
        self().setItem(slot, stack);
    }

    /**
     * Perform additional logic during the transaction, <strong>immediately</strong> after a successful transfer
     * (i.e. {@linkplain ResourceHandler#insert(int, Resource, int, TransactionContext) insert} or
     * {@linkplain ResourceHandler#extract(int, Resource, int, TransactionContext) extract} with result > 0).
     * Any logic performed by this method should be fully transactional, and support being rolled back.
     * In other words, the transaction is still ongoing.
     *
     * @param amountChange If positive, the amount of items that were just inserted into this slot.
     *                     If negative, <strong>minus</strong> the amount of items that were just extracted from this slot.
     * @implSpec Any logic performed by this method must properly support {@linkplain Transaction transactions}.
     *           Note that {@link SnapshotJournal} can serve as the base class for transaction-aware state management.
     */
    @ApiStatus.OverrideOnly
    default void onTransfer(int slot, int amountChange, TransactionContext transaction) {}
}
