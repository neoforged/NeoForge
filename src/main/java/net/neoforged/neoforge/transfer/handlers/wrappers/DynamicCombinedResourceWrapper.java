/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.EmptyHandler;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * Wraps a set of handlers to handle each as if it was a contiguous resource handler blob. This should also be only used as a last resort,
 * given the size() lookup is relatively expensive in comparison to normal handlers. While a dynamically sized IResourceHandler is a valid use case,
 * it should be weighed if wrapping it is the right approach.
 * <p>
 * <strong>Important: This will work with constant sized handlers, but ensure what you are wrapping is dynamically sized.</strong>
 *
 * @param <T>
 */
public class DynamicCombinedResourceWrapper<T extends IResource> implements IResourceHandler<T> {
    private final EmptyHandler<T> emptyHandler;
    protected final IResourceHandler<T>[] handlers; // the handlers

    @SafeVarargs
    public DynamicCombinedResourceWrapper(EmptyHandler<T> emptyHandler, IResourceHandler<T>... handlers) {
        this.emptyHandler = emptyHandler;
        this.handlers = handlers;
    }

    // returns the handler index for the slot or throws if out of bounds
    protected int getHandlerIndex(int slot) {
        var offset = 0;
        for (int i = 0; i < handlers.length; i++) {
            var handler = handlers[i];
            var handlerSize = handler.size();
            if (slot >= offset && slot < handlerSize + offset) {
                return i;
            }
            offset += handlerSize;
        }
        throw new IndexOutOfBoundsException("Index out of bounds. Passed in [%d], but should have been within [0, %d]".formatted(slot, size()));
    }

    protected IResourceHandler<T> getHandlerFromIndex(int index) {
        return index >= 0 && index < handlers.length ? handlers[index] : emptyHandler;
    }

    protected int getSlotFromIndex(int index, int handlerIndex) {
        var sizeUntil = 0;
        //gets all total length up to the index we are in.
        for (int i = 0; i < handlerIndex; i++) {
            sizeUntil += handlers[i].size();
        }
        return index - sizeUntil;
    }

    @Override
    public int size() {
        var sum = 0;
        for (var handler : handlers) {
            sum += handler.size();
        }
        return sum;
    }

    @Override
    public T getResource(int index) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getResource(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public int getAmount(int index) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getAmount(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public int getCapacity(int index) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getCapacity(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public int getCapacity(int index, T resource) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).getCapacity(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).isValid(getSlotFromIndex(index, handlerIndex), resource);
    }

    @Override
    public boolean allowsInsertion(int index) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).allowsInsertion(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public boolean allowsExtraction(int index) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).allowsExtraction(getSlotFromIndex(index, handlerIndex));
    }

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).insert(getSlotFromIndex(index, handlerIndex), resource, amount, action);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        var handled = 0;
        for (var resourceHandler : handlers) {
            handled += resourceHandler.insert(resource, amount - handled, action);
            if (handled >= amount) break;
        }
        return handled;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        var handlerIndex = getHandlerIndex(index);
        return getHandlerFromIndex(handlerIndex).extract(getSlotFromIndex(index, handlerIndex), resource, amount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        var handled = 0;
        for (var resourceHandler : handlers) {
            handled += resourceHandler.extract(resource, amount - handled, action);
            if (handled >= amount) break;
        }
        return handled;
    }

    public static class Modifiable<T extends IResource> extends DynamicCombinedResourceWrapper<T> implements IResourceHandlerModifiable<T> {
        @SafeVarargs
        public Modifiable(EmptyHandler<T> emptyHandler, IResourceHandlerModifiable<T>... handlers) {
            super(emptyHandler, handlers);
        }

        @Override
        public void set(int index, T resource, int amount) {
            var handlerIndex = getHandlerIndex(index);
            var handler = getHandlerFromIndex(handlerIndex);
            if (handler instanceof IResourceHandlerModifiable<T> modifiable)
                modifiable.set(getSlotFromIndex(index, handlerIndex), resource, amount);
        }
    }
}
