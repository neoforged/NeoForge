/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.energy;

import java.util.function.Supplier;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An energy handler that will apply additional per-insert and per-extract limits to another handler.
 */
public class LimitingEnergyHandler extends DelegatingEnergyHandler {
    protected int maxInsert, maxExtract;

    /**
     * Creates a new limiting energy handler.
     *
     * @see LimitingEnergyHandler(Supplier, int, int)
     */
    public LimitingEnergyHandler(EnergyHandler delegate, int maxInsert, int maxExtract) {
        this(() -> delegate, maxInsert, maxExtract);
    }

    /**
     * Creates a new limiting energy handler.
     *
     * @param delegate   energy handler to wrap
     * @param maxInsert  maximum amount of energy that can be inserted in one operation. Can be 0 to disallow insertion entirely.
     * @param maxExtract maximum amount of energy that can be extracted in one operation. Can be 0 to disallow extraction entirely.
     */
    public LimitingEnergyHandler(Supplier<EnergyHandler> delegate, int maxInsert, int maxExtract) {
        super(delegate);
        TransferPreconditions.checkNonNegative(maxInsert);
        TransferPreconditions.checkNonNegative(maxExtract);

        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int toInsert = Math.min(amount, maxInsert);
        return toInsert <= 0 ? 0 : super.insert(toInsert, transaction);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        int toExtract = Math.min(amount, maxExtract);
        return toExtract <= 0 ? 0 : super.extract(toExtract, transaction);
    }
}
