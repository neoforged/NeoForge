/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * Wraps a set of handlers to handle each as if it was a contiguous resource handler blob. This should also be only used as a last resort,
 * given the size() lookup is relatively expensive in comparison to normal handlers. While a dynamically sized IResourceHandler is a valid use case in some situations,
 * it should be weighed if wrapping it is the right approach.
 * <p>
 * <strong>Important: While this will work with constant sized handlers, it is advised to make sure you are using this version with at least one dynamically sized handler.</strong>
 * If none of the handlers wrapped are resizeable, then please use the static sized version {@link CombinedResourceHandlerWrapper}
 * <p>
 * It is also important that the size should not change until after the transaction is finished.
 * <p>
 * <b>By itself, this does not handle snapshotting.</b> It is expected the delegated handlers take care of what needs to be journaled.
 *
 * @param <T>
 */
public class ResizableCombinedResourceHandlerWrapper<T extends IResource> extends CombinedResourceHandlerWrapper<T> {
    @SafeVarargs
    public ResizableCombinedResourceHandlerWrapper(IResourceHandler<T>... handlers) {
        super(handlers);
    }

    @Override
    public int size() {
        int sum = 0;
        for (IResourceHandler<T> handler : handlers) {
            sum += handler.size();
        }
        return sum;
    }

    @Override
    protected int getHandlerIndex(int index) {
        int offset = 0;
        for (int i = 0; i < handlers.length; i++) {
            IResourceHandler<T> handler = handlers[i];
            int handlerSize = handler.size();
            if (index >= offset && index < handlerSize + offset) {
                return i;
            }
            offset += handlerSize;
        }
        throw new IndexOutOfBoundsException("Index out of bounds. Passed in [%d], but should have been within [0, %d]".formatted(index, size()));
    }

    @Override
    protected int getSlotFromIndex(int index, int handlerIndex) {
        int sizeUntil = 0;
        //gets all total length up to the index we are in.
        for (int i = 0; i < handlerIndex; i++) {
            sizeUntil += handlers[i].size();
        }
        return index - sizeUntil;
    }

    public static class Modifiable<T extends IResource> extends ResizableCombinedResourceHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
        @SafeVarargs
        public Modifiable(IResourceHandlerModifiable<T>... handlers) {
            super(handlers);
        }

        @Override
        public void set(int index, T resource, int amount) {
            int handlerIndex = getHandlerIndex(index);
            IResourceHandler<T> handler = getHandlerFromIndex(handlerIndex);
            if (handler instanceof IResourceHandlerModifiable<T> modifiable)
                modifiable.set(getSlotFromIndex(index, handlerIndex), resource, amount);
        }
    }
}
