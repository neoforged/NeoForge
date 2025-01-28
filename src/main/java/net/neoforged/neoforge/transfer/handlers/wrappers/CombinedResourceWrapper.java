package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.EmptyHandler;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * Wraps a set of handlers to handle each as if it was a contiguous resource handler blob.
 * <p>
 * <strong>Important: This will only work with constant sized handlers.</strong>
 * Dynamically sized handlers are supported by api, but not by this implementation
 * @param <T>
 */
public class CombinedResourceWrapper<T extends IResource> implements IResourceHandler<T> {
    private final EmptyHandler<T> emptyHandler;
    protected final IResourceHandler<T>[] handlers; // the handlers
    protected final int[] baseIndex; // index-offsets of the different handlers
    protected final int sizeCache; // number of total slots

    @SafeVarargs
    public CombinedResourceWrapper(EmptyHandler<T> emptyHandler, IResourceHandler<T>... handlers) {
        this.emptyHandler = emptyHandler;
        this.handlers = handlers;
        this.baseIndex = new int[handlers.length];
        int index = 0;
        for (int i = 0; i < handlers.length; i++) {
            index += handlers[i].size();
            baseIndex[i] = index;
        }
        this.sizeCache = index;
    }

    // returns the handler index for the slot
    protected int getHandlerIndex(int slot) {
        if (slot < 0)
            return -1;

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    protected IResourceHandler<T> getHandlerFromIndex(int index) {
        return index >= 0 && index < handlers.length ? handlers[index] : emptyHandler;
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

    public static class Modifiable<T extends IResource> extends CombinedResourceWrapper<T> implements IResourceHandlerModifiable<T> {
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
