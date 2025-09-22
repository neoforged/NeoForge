/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A buffer of energy that accepts any and all energy inserted into it, but never has an extractable amount.
 * If you need custom behaviour, then a new implementation is required rather than extending {@link VoidEnergyHandler}
 */
public final class VoidEnergyHandler implements EnergyHandler {
    public static final VoidEnergyHandler INSTANCE = new VoidEnergyHandler();

    @Override
    public long getAmountAsLong() {
        return 0;
    }

    @Override
    public long getCapacityAsLong() {
        return Long.MAX_VALUE;
    }

    //Accepts as much as is inserted
    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return amount;
    }

    //Never has anything to extract so we return 0
    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }

    private VoidEnergyHandler() {}
}
