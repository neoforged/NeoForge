/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.energy;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Range;

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

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int size() {
        return handler.size();
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getAmount(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return handler.getAmount(index);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getCapacity(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return handler.getCapacity(index);
    }

    public boolean supportsInsertion(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return handler.supportsInsertion(index);
    }

    public boolean supportsInsertion() {
        return handler.supportsInsertion();
    }

    public boolean supportsExtraction(@Range(from = 0, to = Integer.MAX_VALUE) int index) {
        return handler.supportsExtraction(index);
    }

    public boolean supportsExtraction() {
        return handler.supportsExtraction();
    }

    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(index, amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int inserted = handler.insert(amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int index, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int extracted = handler.extract(index, amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }

    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransferAction actionType) {
        try (Transaction transaction = Transaction.open(null)) {
            int extracted = handler.extract(amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }
}
