/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Wraps a set of handlers to handle each as if it was a contiguous resource handler blob.
 * <p>
 * <strong>Important: This will only work with constant sized handlers.</strong>
 * Dynamically sized handlers are supported by api, but not by this implementation
 * <p>
 * <b>By itself, this does not handle snapshotting.</b> It is expected the delegated handlers take care of what needs to be journaled. *
 *
 * @param <T>
 */
public class CombinedResourceHandlerWrapper<T extends IResource> implements IResourceHandler<T> {
    protected final IResourceHandler<T>[] handlers; // the handlers
    protected final int[] baseIndex; // index-offsets of the different handlers
    protected final int sizeCache; // number of total slots

    @SafeVarargs
    public CombinedResourceHandlerWrapper(IResourceHandler<T>... handlers) {
        if (handlers.length <= 1) throw new IllegalArgumentException("At least 2 handlers must be specified");
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
        if (index < 0) throw new IndexOutOfBoundsException(index);

        for (int i = 0; i < baseIndex.length; i++) {
            if (index - baseIndex[i] < 0) {
                return i;
            }
        }
        throw new IndexOutOfBoundsException(index);
    }

    protected IResourceHandler<T> getHandlerFromIndex(int index) {
        if (index >= 0 && index < handlers.length)
            return handlers[index];

        // Probably log something here for the user to know, but we likely shouldn't crash given this is cross mod support.
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
    public int getAmount(int index) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getAmount(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public long getAmountAsLong(int index) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getAmountAsLong(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public int getCapacity(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getCapacity(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public long getCapacityAsLong(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getCapacityAsLong(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).isValid(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public int characteristics(int index) {
        int handlerIndex = getHandlerIndex(index);
        var specifiedIndex = getSlotFromIndex(index, handlerIndex);
        return getHandlerFromIndex(handlerIndex).characteristics(specifiedIndex);
    }

    @Override
    public int characteristics() {
        int handled = TransferCharacteristics.UNKNOWN;
        for (IResourceHandler<T> resourceHandler : handlers) {
            handled |= resourceHandler.characteristics();
        }
        return handled;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).insert(getSlotFromIndex(index, handlerIndex), resource, amount, transaction);
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        for (IResourceHandler<T> resourceHandler : handlers) {
            handled += resourceHandler.insert(resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).extract(getSlotFromIndex(index, handlerIndex), resource, amount, transaction);
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        for (IResourceHandler<T> resourceHandler : handlers) {
            handled += resourceHandler.extract(resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }
}
