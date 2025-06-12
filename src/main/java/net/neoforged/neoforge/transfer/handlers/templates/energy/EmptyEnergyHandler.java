/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class EmptyEnergyHandler implements IEnergyHandler {
    public static final IEnergyHandler INSTANCE = new EmptyEnergyHandler();

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int getAmount(int index) {
        return 0;
    }

    @Override
    public int getCapacity(int index) {
        return 0;
    }

    @Override
    public boolean supportsInsertion(int index) {
        return false;
    }

    @Override
    public boolean supportsExtraction(int index) {
        return false;
    }

    @Override
    public int insert(int index, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int index, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
