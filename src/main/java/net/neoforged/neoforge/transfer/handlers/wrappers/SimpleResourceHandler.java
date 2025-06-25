/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.Objects;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.UnsafeTransactionManager;

/**
 * A simple resource handler that wraps an {@link IResourceHandler} and provides a simplified interface without
 * needing to manually start or stop a transaction.
 * <p>
 * This is intended for use in simple cases where you do not need the full power of the transaction system. It is important to remember, that this is not an IResourceHandler itself.
 */
public final class SimpleResourceHandler<T extends IResource> {
    //todo copy all docs over from the existing handler interface before final reviews.
    // During review process we may change the documentation of IResourceHandler, so it is simpler to do it all in the end.
    public static <T extends IResource> SimpleResourceHandler<T> of(IResourceHandler<T> handler) {
        return new SimpleResourceHandler<>(handler);
    }

    private final IResourceHandler<T> handler;

    private SimpleResourceHandler(IResourceHandler<T> handler) {
        this.handler = handler;
    }

    public int size() {
        return handler.size();
    }

    public T getResource(int index) {
        return handler.getResource(index);
    }

    public int getAmount(int index) {
        return handler.getAmount(index);
    }

    public long getAmountAsLong(int index) {
        return handler.getAmountAsLong(index);
    }

    public int getCapacity(int index, T resource) {
        return handler.getCapacity(index, resource);
    }

    public long getCapacityAsLong(int index, T resource) {
        return handler.getCapacityAsLong(index, resource);
    }

    public boolean isValid(int index, T resource) {
        return handler.isValid(index, resource);
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

    public int insert(int index, T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int inserted = handler.insert(index, resource, amount, transaction);
            if (inserted > 0) actionType.commit(transaction);
            return inserted;
        }
    }

    public int insert(T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int inserted = handler.insert(resource, amount, transaction);
            if (inserted > 0) actionType.commit(transaction);
            return inserted;
        }
    }

    public int extract(int index, T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {

            int extracted = handler.extract(index, resource, amount, transaction);
            if (extracted > 0) actionType.commit(transaction);
            return extracted;
        }
    }

    public int extract(T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = UnsafeTransactionManager.openUnsafe()) {
            int extracted = handler.extract(resource, amount, transaction);
            if (extracted > 0) actionType.commit(transaction);
            return extracted;
        }
    }

    public IResourceHandler<T> handler() {
        return handler;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleResourceHandler<?>) obj;
        return Objects.equals(this.handler, that.handler);
    }

    @Override
    public int hashCode() {
        return handler.hashCode();
    }

    @Override
    public String toString() {
        return "SimpleResourceHandler[" +
                "handler=" + handler + ']';
    }
}
