/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A buffer of energy that accepts any and all energy inserted into it, but never has an extractable amount.
 * If you need custom behaviour, then a new implementation is required rather than extending {@link VoidEnergyHandler}
 */
public final class VoidEnergyHandler implements IEnergyHandler {
    public static final VoidEnergyHandler INSTANCE = new VoidEnergyHandler();

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong() {
        return Long.MAX_VALUE;
    }

    //Accepts as much as is inserted
    @Override
    public int insert(int amount, TransactionContext transaction) {
        return TransferPreconditions.checkNonNegative(amount);
    }

    //Never has anything to extract so we return 0
    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.INSERTABLE | TransferCharacteristics.VOIDING | TransferCharacteristics.IMMUTABLE;
    }

    /**
     * Any custom implementations are expected to make their own full implementation rather than extend {@link VoidEnergyHandler}
     */
    private VoidEnergyHandler() {}
}
