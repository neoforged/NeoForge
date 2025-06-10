/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import javax.annotation.Nonnegative;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class EmptyEnergyHandler implements IEnergyHandler {
    public static final IEnergyHandler INSTANCE = new EmptyEnergyHandler();

    @Override
    @Nonnegative
    public int size() {
        return 0;
    }

    @Override
    @Nonnegative
    public int getAmount(@Nonnegative int index) {
        return 0;
    }

    @Override
    @Nonnegative
    public int getCapacity(@Nonnegative int index) {
        return 0;
    }

    @Override
    public boolean supportsInsertion(@Nonnegative int index) {
        return false;
    }

    @Override
    public boolean supportsExtraction(@Nonnegative int index) {
        return false;
    }

    @Override
    @Nonnegative
    public int insert(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    @Nonnegative
    public int insert(@Nonnegative int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    @Nonnegative
    public int extract(@Nonnegative int index, @Nonnegative int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    @Nonnegative
    public int extract(@Nonnegative int amount, TransactionContext transaction) {
        return 0;
    }
}
