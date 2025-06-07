/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.energy.ISingleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * A buffer of energy that accepts any and all energy inserted into it, but never has an extractable amount.
 * If you need custom behaviour, then a new implementation is required rather than extending {@link VoidEnergyHandler}
 */
public final class VoidEnergyHandler implements ISingleEnergyHandler {
    public static final IEnergyHandler INSTANCE = new VoidEnergyHandler();

    //Never has any energy
    @Override
    @Range(from = 0, to = 0)
    public int getAmount(int index) {
        return 0;
    }

    //Has a capacity of infinite energy
    @Override
    public int getCapacity(int index) {
        return Integer.MAX_VALUE;
    }

    //Always accepts energy
    @Override
    public boolean supportsInsertion() {
        return true;
    }

    //Voids do not provide energy
    @Override
    public boolean supportsExtraction() {
        return false;
    }

    //Accepts as much as is inserted, but rather than calling the above method, it is just simpler to return the amount.
    @Override
    public int insert(int amount, TransactionContext transaction) {
        return amount;
    }

    //Never has anything to extract so we return 0
    @Override
    @Range(from = 0, to = 0)
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    /**
     * Any custom implementations are expected to make their own full implementation rather than extend {@link VoidEnergyHandler}
     */
    private VoidEnergyHandler() {}
}
