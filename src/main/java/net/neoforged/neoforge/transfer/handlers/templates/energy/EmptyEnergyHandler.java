/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import net.neoforged.neoforge.transfer.handlers.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class EmptyEnergyHandler implements EnergyHandler {
    public static final EmptyEnergyHandler INSTANCE = new EmptyEnergyHandler();

    private EmptyEnergyHandler() {}

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return 0;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
