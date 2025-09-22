/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import java.util.Objects;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An energy handler that will apply additional per-insert and per-extract limits to another handler.
 */
public class LimitingEnergyHandler implements EnergyHandler {
    protected final EnergyHandler delegate;
    protected final int maxInsert, maxExtract;

    /**
     * Creates a new limiting energy handler.
     *
     * @param delegate   energy handler to wrap
     * @param maxInsert  maximum amount of energy that can be inserted in one operation. Can be 0 to disallow insertion entirely.
     * @param maxExtract maximum amount of energy that can be extracted in one operation. Can be 0 to disallow extraction entirely.
     */
    public LimitingEnergyHandler(EnergyHandler delegate, int maxInsert, int maxExtract) {
        Objects.requireNonNull(delegate);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);

        this.delegate = delegate;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    @Override
    public long getAmountAsLong() {
        return delegate.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return delegate.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return delegate.insert(Math.min(amount, maxInsert), transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return delegate.extract(Math.min(amount, maxExtract), transaction);
    }
}
