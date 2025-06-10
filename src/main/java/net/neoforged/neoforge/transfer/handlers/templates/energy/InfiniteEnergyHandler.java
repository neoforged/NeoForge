/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.energy;

import javax.annotation.Nonnegative;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.handlers.energy.ISingleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A buffer of energy that has an endless supply, but never insertable.
 * If you need custom behaviour, then a new implementation is required rather than extending {@link InfiniteEnergyHandler}
 * <p>
 * <b>Note</b> This does not auto eject by just having this as your capability, this is just a buffer.
 */
public final class InfiniteEnergyHandler implements ISingleEnergyHandler {
    public static final IEnergyHandler INSTANCE = new InfiniteEnergyHandler();

    //Always has Max value available
    @Override
    @Nonnegative
    public int getAmount(@Nonnegative int index) {
        return Integer.MAX_VALUE;
    }

    //Holds "infinite" energy, thus always has Max value available
    @Override
    @Nonnegative
    public int getCapacity(@Nonnegative int index) {
        return Integer.MAX_VALUE;
    }

    //Never
    @Override
    public boolean supportsInsertion() {
        return false;
    }

    //Always
    @Override
    public boolean supportsExtraction() {
        return true;
    }

    //Ignores the amount attempted to be inserted
    @Override
    @Nonnegative
    public int insert(@Nonnegative int amount, TransactionContext transaction) {
        return 0;
    }

    //Allows extraction to the exact amount specified
    @Override
    @Nonnegative
    public int extract(@Nonnegative int amount, TransactionContext transaction) {
        return amount;
    }

    /**
     * Any custom implementations are expected to make their own full implementation rather than extend {@link InfiniteEnergyHandler}
     */
    private InfiniteEnergyHandler() {}
}
