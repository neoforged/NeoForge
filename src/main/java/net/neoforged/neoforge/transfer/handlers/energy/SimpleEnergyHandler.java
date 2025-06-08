/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import javax.annotation.Nonnegative;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * A utility class that wraps an {@link IEnergyHandler} and provides a simplified interface without
 * the use of transactions
 * <p>
 * This is intended for use in simple cases where you do not need the full power of the transaction system
 */
public final class SimpleEnergyHandler {
    public static SimpleEnergyHandler of(IEnergyHandler handler) {
        return new SimpleEnergyHandler(handler);
    }

    private final IEnergyHandler handler;

    public SimpleEnergyHandler(IEnergyHandler handler) {
        this.handler = handler;
    }

    @Nonnegative
    public int size() {
        return handler.size();
    }

    @Nonnegative
    public int getAmount(@Nonnegative int index) {
        return handler.getAmount(index);
    }

    @Nonnegative
    public int getCapacity(@Nonnegative int index) {
        return handler.getCapacity(index);
    }

    public boolean supportsInsertion(@Nonnegative int index) {
        return handler.supportsInsertion(index);
    }

    public boolean supportsInsertion() {
        return handler.supportsInsertion();
    }

    public boolean supportsExtraction(@Nonnegative int index) {
        return handler.supportsExtraction(index);
    }

    public boolean supportsExtraction() {
        return handler.supportsExtraction();
    }

    public int insert(@Nonnegative int index, @Nonnegative int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(index, amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int insert(@Nonnegative int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int extract(@Nonnegative int index, @Nonnegative int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int extracted = handler.extract(index, amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }

    public int extract(@Nonnegative int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int extracted = handler.extract(amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }
}
