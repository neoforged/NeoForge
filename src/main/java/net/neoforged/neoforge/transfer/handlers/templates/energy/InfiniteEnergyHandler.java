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
 * A buffer of energy that has an endless supply, but never insertable.
 * If you need custom behaviour, then a new implementation is required rather than extending {@link InfiniteEnergyHandler}
 * <p>
 * <b>Note</b> This does not auto eject by just having this as your capability, this is just a buffer.
 */
public final class InfiniteEnergyHandler implements IEnergyHandler {
    public static final InfiniteEnergyHandler INSTANCE = new InfiniteEnergyHandler();

    @Override
    public int getAmount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getAmountAsLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.STATICALLY_SIZED | TransferCharacteristics.EXTRACTABLE | TransferCharacteristics.INFINITE | TransferCharacteristics.IMMUTABLE;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return TransferPreconditions.checkNonNegative(amount);
    }

    private InfiniteEnergyHandler() {}
}
