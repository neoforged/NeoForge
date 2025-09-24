/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import java.util.Objects;
import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An energy handler that delegates all calls to another handler.
 */
public class DelegatingEnergyHandler implements EnergyHandler {
    protected final Supplier<EnergyHandler> delegate;

    public DelegatingEnergyHandler(EnergyHandler delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public DelegatingEnergyHandler(Supplier<EnergyHandler> delegate) {
        this.delegate = delegate;
    }

    @Override
    public long getAmountAsLong() {
        return getDelegate().getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {
        return getDelegate().getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return getDelegate().insert(amount, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        return getDelegate().extract(amount, transaction);
    }

    public EnergyHandler getDelegate() {
        return delegate.get();
    }
}
