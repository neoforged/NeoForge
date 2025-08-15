/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A resource handler that wraps multiple resource handlers, concatenating all their indices into one large handler.
 * <p>
 * <strong>This wrapper assumes that all internal handlers have a constant size.</strong>
 */
public class CombinedResourceHandlerWrapper<T extends IResource> implements IResourceHandler<T> {
    protected final IResourceHandler<T>[] handlers; // the handlers
    protected final int[] baseIndex; // index-offsets of the different handlers
    protected final int sizeCache; // number of total indices

    @SafeVarargs
    public CombinedResourceHandlerWrapper(IResourceHandler<T>... handlers) {
        if (handlers.length <= 1) throw new IllegalArgumentException("At least 2 handlers must be specified. Received: " + handlers.length);
        this.handlers = handlers;
        this.baseIndex = new int[handlers.length];
        int index = 0;
        for (int i = 0; i < handlers.length; i++) {
            index += handlers[i].size();
            baseIndex[i] = index;
        }
        this.sizeCache = index;
    }

    // returns the handler index for the index
    protected int getHandlerIndex(int index) {
        if (index < 0) throw new IndexOutOfBoundsException("Index " + index + " is out-of-bounds for combined handler with size " + sizeCache);

        for (int i = 0; i < baseIndex.length; i++) {
            if (index - baseIndex[i] < 0) {
                return i;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    protected IResourceHandler<T> getHandlerFromIndex(int index) {
        if (index >= 0 && index < handlers.length)
            return handlers[index];

        throw new IndexOutOfBoundsException(index);
    }

    protected int getSlotFromIndex(int index, int handlerIndex) {
        return handlerIndex > 0 && handlerIndex < baseIndex.length ? index - baseIndex[handlerIndex - 1] : index;
    }

    @Override
    public int size() {
        return sizeCache;
    }

    @Override
    public T getResource(int index) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getResource(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public long getAmount(int index) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getAmount(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public long getCapacity(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getCapacity(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        if (resource.isEmpty()) return true;
        return getHandlerFromIndex(handlerIndex).isValid(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).insert(getSlotFromIndex(index, handlerIndex), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int inserted = 0;
        for (IResourceHandler<T> resourceHandler : handlers) {
            inserted += resourceHandler.insert(resource, amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).extract(getSlotFromIndex(index, handlerIndex), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int extracted = 0;
        for (IResourceHandler<T> resourceHandler : handlers) {
            extracted += resourceHandler.extract(resource, amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return extracted;
    }
}
