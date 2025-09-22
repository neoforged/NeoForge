/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An empty energy handler.
 * <p>It has no stored energy, no capacity, and rejects insertions and extractions.
 */
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
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }
}
