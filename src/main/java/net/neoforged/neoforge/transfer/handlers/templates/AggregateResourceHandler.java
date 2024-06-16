/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

import java.util.stream.Stream;

/**
 * An {@link IResourceHandler} that aggregates multiple handlers into one.
 * @param <T> The type of resource
 */
public class AggregateResourceHandler<T extends IResource> implements IResourceHandler<T> {
    protected final IResourceHandler<T>[] handlers;

    @SafeVarargs
    public AggregateResourceHandler(IResourceHandler<T>... handlers) {
        this.handlers = handlers;
    }

    @Override
    public int size() {
        return Stream.of(getHandlers()).mapToInt(IResourceHandler::size).sum();
    }

    @Override
    public T getResource(int index) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.getResource(index);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getAmount(int index) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.getAmount(index);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getLimit(int index, T resource) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.getLimit(index, resource);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean isValid(int index, T resource) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.isValid(index, resource);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean canInsert() {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (storage.canInsert()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canExtract() {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (storage.canExtract()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                if (storage.canInsert()) {
                    return storage.insert(index, resource, amount, action);
                } else {
                    return 0;
                }
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (IResourceHandler<T> storage : getHandlers()) {
            if (storage.canInsert()) {
                inserted += storage.insert(resource, amount - inserted, action);
            }
            if (inserted >= amount) {
                return inserted;
            }
        }
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                if (storage.canExtract()) {
                    return storage.extract(index, resource, amount, action);
                } else {
                    return 0;
                }
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        int extracted = 0;
        for (IResourceHandler<T> storage : getHandlers()) {
            if (storage.canExtract()) {
                extracted += storage.extract(resource, amount - extracted, action);
            }
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }

    public IResourceHandler<T>[] getHandlers() {
        return handlers;
    }

    public static class Modifiable<T extends IResource> extends AggregateResourceHandler<T> implements IResourceHandlerModifiable<T> {
        @SafeVarargs
        public Modifiable(IResourceHandlerModifiable<T>... handlers) {
            super(handlers);
        }

        @Override
        public void set(int index, T resource, int amount) {
            for (IResourceHandlerModifiable<T> storage : getHandlers()) {
                if (index < storage.size()) {
                    storage.set(index, resource, amount);
                    return;
                }
                index -= storage.size();
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public IResourceHandlerModifiable<T>[] getHandlers() {
            return (IResourceHandlerModifiable<T>[]) handlers;
        }
    }
}
