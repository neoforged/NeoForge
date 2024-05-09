package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.storage.IStorage;

import java.util.stream.Stream;

public class AggregateStorage<T> implements IStorage<T> {
    IStorage<T>[] storages;

    public AggregateStorage(IStorage<T>... storages) {
        this.storages = storages;
    }

    @Override
    public int getSlots() {
        return Stream.of(storages).mapToInt(IStorage::getSlots).sum();
    }

    @Override
    public T getResource(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                return storage.getResource(slot);
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getAmount(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                return storage.getAmount(slot);
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int getSlotLimit(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                return storage.getSlotLimit(slot);
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean isResourceValid(int slot, T resource) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                return storage.isResourceValid(slot, resource);
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean isEmpty(int slot) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                return storage.isEmpty(slot);
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean allowsInsertion() {
        for (IStorage<T> storage : storages) {
            if (storage.allowsInsertion()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean allowsExtraction() {
        for (IStorage<T> storage : storages) {
            if (storage.allowsExtraction()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int insert(int slot, T resource, int amount, TransferAction action) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                if (storage.allowsInsertion()) {
                    return storage.insert(slot, resource, amount, action);
                } else {
                    return 0;
                }
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (IStorage<T> storage : storages) {
            if (storage.allowsInsertion()) {
                inserted += storage.insert(resource, amount - inserted, action);
            }
            if (inserted >= amount) {
                return inserted;
            }
        }
        return inserted;
    }

    @Override
    public int extract(int slot, T resource, int amount, TransferAction action) {
        for (IStorage<T> storage : storages) {
            if (slot < storage.getSlots()) {
                if (storage.allowsExtraction()) {
                    return storage.extract(slot, resource, amount, action);
                } else {
                    return 0;
                }
            }
            slot -= storage.getSlots();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        int extracted = 0;
        for (IStorage<T> storage : storages) {
            if (storage.allowsExtraction()) {
                extracted += storage.extract(resource, amount - extracted, action);
            }
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }
}
