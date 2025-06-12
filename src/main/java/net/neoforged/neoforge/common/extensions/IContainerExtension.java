/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Extension methods for {@link Container}. These methods are used by {@link VanillaContainerWrapper},
 * to allow containers to be integrated in a transaction.
 */
public interface IContainerExtension {
    private Container self() {
        return (Container) this;
    }

    /**
     * An extension of {@link Container#setItem(int, ItemStack)} that allows disabling committed actions.
     *
     * <p>If {@code performCommitActions} is {@code false}, changes (e.g. calling {@code setChanged} or making changes to the world) should be deferred until after the performCommitActions.
     */
    default void setItem(int slot, ItemStack stack, boolean performCommitActions) {
        self().setItem(slot, stack);
    }

    /**
     * Perform changes that were deferred in {@link #setItem(int, ItemStack, boolean)}
     * because {@code forceChanges} was false.
     *
     * <p>There is no need to call {@code setChanged}, as it is already called by {@link VanillaContainerWrapper}.
     */
    default void onCommit(int slot, ItemStack originalStack) {}

    /**
     * Perform additional logic during the transaction after a transfer of at least 1 in value. (i.e. insert or extract with result > 0).
     * Any logic performed here should be fully transactional, and support being rolled back.
     * In other words, the transaction is still on going.
     */
    default void onTransfer(int slot, IODirection ioDirection, TransactionContext context) {}

    enum IODirection {
        INSERT,
        EXTRACT
    }
}
