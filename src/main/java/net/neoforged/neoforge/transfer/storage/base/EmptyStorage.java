/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.storage.base;

import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An immutable, empty storage.
 */
public final class EmptyStorage<T> implements Storage<T> {
    private final static EmptyStorage<?> INSTANCE = new EmptyStorage<>();

    public static <T> EmptyStorage<T> instance() {
        //noinspection unchecked
        return (EmptyStorage<T>) INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    private RuntimeException invalidSlot(int slot) {
        throw new IllegalArgumentException("Invalid slot index: " + slot + ". This storage is empty and has no slots.");
    }

    @Override
    public long insert(int slot, T resource, long maxAmount, TransactionContext transaction) {
        throw invalidSlot(slot);
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long extract(int slot, T resource, long maxAmount, TransactionContext transaction) {
        throw invalidSlot(slot);
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public boolean isResourceBlank(int slot) {
        throw invalidSlot(slot);
    }

    @Override
    public T getResource(int slot) {
        throw invalidSlot(slot);
    }

    @Override
    public long getAmount(int slot) {
        throw invalidSlot(slot);
    }

    @Override
    public long getCapacity(int slot, T resource) {
        throw invalidSlot(slot);
    }

    @Override
    public boolean isValid(int slot, T resource) {
        throw invalidSlot(slot);
    }
}
