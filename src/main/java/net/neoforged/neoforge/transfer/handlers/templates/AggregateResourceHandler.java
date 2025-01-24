/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

/**
 * An {@link IResourceHandler} that aggregates multiple handlers into one.
 * @param <T> The type of resource
 */
//What is the use case? As there are some assumptions here that may be leading to wacky results

public class AggregateResourceHandler<T extends IResource> implements IResourceHandler<T> {
    protected final IResourceHandler<T>[] handlers;

    @SafeVarargs
    public AggregateResourceHandler(IResourceHandler<T>... handlers) {
        this.handlers = handlers;
    }

    @Override
    public int size() {
        //Let's avoid the allocation of a stream. We may want to consider if size() can be constant. Things that could potentially cause this to not be possible: handlers existing on items
        int sum = 0;
        for (IResourceHandler<T> resourceHandler : getHandlers()) {
            sum += resourceHandler.size();
        }
        return sum;
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
    public int getCapacity(int index, T resource) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.getCapacity(index, resource);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getCapacity(int index) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.getCapacity(index);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }
    @Override
    public boolean isValid(T resource) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (storage.isValid(resource)) {
                return true;
            }
        }
        return false;
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
    public boolean allowsInsertion(int index) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.allowsInsertion(index);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean allowsExtraction(int index) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                return storage.allowsExtraction(index);
            }
            index -= storage.size();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                inserted += storage.insert(index, resource, amount - inserted, action);
            }
            if (inserted >= amount)
                break;
        }

        return inserted;


        //What? lol this would have stopped on the first one that said "return 0"
//        for (IResourceHandler<T> storage : getHandlers()) {
//            if (index < storage.size()) {
//                //`allowsInsertion` is a hint, it is not meant for logic control per se. This is too late to call this.
//                // "Checks if the given index allows insertion of a resource, regardless of the state of the handler." so in this scenario we are wasting time checking it again.
//                // Insert should handle the "can't insert" call pretty rapidly on a given handler so we don't need to inquire here
//                //This also checks for the entire storage rather than the index, which is also misleading
//                //If we NEED this guaranteed, which we shouldn't, then we should instead take note of all the handlers that say allows when setting the handler list rather than every time we go to insert.
////                if (storage.allowsInsertion()) {
//                    return storage.insert(index, resource, amount, action);
////                } else {
////                    return 0;
////                }
//            }
//            index -= storage.size();
//        }
//        throw new IndexOutOfBoundsException();
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (IResourceHandler<T> storage : getHandlers()) {
//            if (storage.allowsInsertion()) {
                inserted += storage.insert(resource, amount - inserted, action);
//            }
            if (inserted >= amount)
                break;
        }
        return inserted;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        for (IResourceHandler<T> storage : getHandlers()) {
            if (index < storage.size()) {
                if (storage.allowsExtraction()) {
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
//            if (storage.allowsExtraction()) {
            extracted += storage.extract(resource, amount - extracted, action);
//            }
            if (extracted >= amount)
                break;
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
