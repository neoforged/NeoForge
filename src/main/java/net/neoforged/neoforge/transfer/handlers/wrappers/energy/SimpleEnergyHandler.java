/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.energy;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;

/**
 * A utility class that wraps an {@link IEnergyHandler} and provides a simplified interface without
 * the use of transactions
 * <p>
 * This is intended for use in simple cases where you do not need the full power of the transaction system. It is important to remember, that this is not an IEnergyHandler itself.
 */
public final class SimpleEnergyHandler {
    public static SimpleEnergyHandler of(IEnergyHandler handler) {
        return new SimpleEnergyHandler(handler);
    }

    private final IEnergyHandler handler;

    private SimpleEnergyHandler(IEnergyHandler handler) {
        this.handler = handler;
    }

    public int getAmount() {
        return handler.getAmount();
    }

    public long getAmountAsLong() {
        return handler.getAmountAsLong();
    }

    public int getCapacity() {
        return handler.getCapacity();
    }

    public long getCapacityAsLong() {
        return handler.getCapacityAsLong();
    }

    public boolean supportsInsertion() {
        return handler.supportsInsertion();
    }

    public boolean supportsExtraction() {
        return handler.supportsExtraction();
    }

    public int insert(int amount, TransferAction actionType) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int inserted = handler.insert(amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int extract(int amount, TransferAction actionType) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int extracted = handler.extract(amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }
}
