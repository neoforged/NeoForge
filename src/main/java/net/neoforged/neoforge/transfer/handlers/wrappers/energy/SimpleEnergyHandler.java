/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.energy;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;

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

    public SimpleEnergyHandler(IEnergyHandler handler) {
        this.handler = handler;
    }

    public int size() {
        return handler.size();
    }

    public int getAmount(int index) {
        return handler.getAmount(index);
    }

    public long getAmountAsLong(int index) {
        return handler.getAmountAsLong(index);
    }

    public int getCapacity(int index) {
        return handler.getCapacity(index);
    }

    public long getCapacityAsLong(int index) {
        return handler.getCapacityAsLong(index);
    }

    public boolean supportsInsertion(int index) {
        return handler.supportsInsertion(index);
    }

    public boolean supportsInsertion() {
        return handler.supportsInsertion();
    }

    public boolean supportsExtraction(int index) {
        return handler.supportsExtraction(index);
    }

    public boolean supportsExtraction() {
        return handler.supportsExtraction();
    }

    public int insert(int index, int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            int inserted = handler.insert(index, amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int insert(int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            int inserted = handler.insert(amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int extract(int index, int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            try (var sub = transaction.open()) {
                //...
            }
            int extracted = handler.extract(index, amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }

    public int extract(int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            int extracted = handler.extract(amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }
}
