/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import net.neoforged.neoforge.transfer.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.energy.ISingleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A buffer of energy that accepts any and all energy inserted into it, but never has an extractable amount.
 * If you need custom behaviour, then a new implementation is required rather than extending {@link VoidEnergyHandler}
 */
public final class VoidEnergyHandler implements ISingleEnergyHandler {
    public static final IEnergyHandler INSTANCE = new VoidEnergyHandler();

    @Override
    public int getAmount(int index) {
        return 0;
    }

    @Override
    public int getCapacity(int index) {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong(int index) {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    //Accepts as much as is inserted
    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        return amount;
    }

    //Never has anything to extract so we return 0
    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (EnergyHandlerUtil.checkEnergy(amount)) return 0;
        return 0;
    }

    /**
     * Any custom implementations are expected to make their own full implementation rather than extend {@link VoidEnergyHandler}
     */
    private VoidEnergyHandler() {}
}
