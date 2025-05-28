package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Base {@link Storage} implementation that concatenates multiple storages into a single one.
 *
 * <p>Note that this class assumes that each sub-storage has a fixed size.
 */
public class CombinedStorage<T> implements Storage<T> {
    protected final Storage<T>[] storages; // the storages
    protected final int[] baseIndex; // index-offsets of the different storages
    protected final int slotCount; // number of total slots

    @SafeVarargs
    public CombinedStorage(Storage<T>... storage) {
        this.storages = storage;
        this.baseIndex = new int[storage.length];
        int index = 0;
        for (int i = 0; i < storage.length; i++) {
            index += storage[i].size();
            baseIndex[i] = index;
        }
        this.slotCount = index;
    }

    // returns the storage index for the slot
    protected int getStorageIndex(int slot) {
        StoragePreconditions.checkSlot(slot, slotCount);

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }

        throw new RuntimeException("Unreachable");
    }

    protected int getStorageSlot(int slot, int storageIndex) {
        return slot - baseIndex[storageIndex - 1];
    }

    @Override
    public int size() {
        return slotCount;
    }

    @Override
    public long insert(int slot, T resource, long maxAmount, TransactionContext transaction) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].insert(getStorageSlot(slot, storageIndex), resource, maxAmount, transaction);
    }

    @Override
    public long insert(T resource, long maxAmount, TransactionContext transaction) {
        long amount = 0;

        for (Storage<T> storage : storages) {
            amount += storage.insert(resource, maxAmount - amount, transaction);
            if (amount == maxAmount) break;
        }

        return amount;
    }

    @Override
    public boolean supportsInsertion() {
        for (Storage<T> storage : storages) {
            if (storage.supportsInsertion()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long extract(int slot, T resource, long maxAmount, TransactionContext transaction) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].extract(getStorageSlot(slot, storageIndex), resource, maxAmount, transaction);
    }

    @Override
    public long extract(T resource, long maxAmount, TransactionContext transaction) {
        long amount = 0;

        for (Storage<T> storage : storages) {
            amount += storage.extract(resource, maxAmount - amount, transaction);
            if (amount == maxAmount) break;
        }

        return amount;
    }

    @Override
    public boolean supportsExtraction() {
        for (Storage<T> storage : storages) {
            if (storage.supportsExtraction()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isResourceBlank(int slot) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].isResourceBlank(getStorageSlot(slot, storageIndex));
    }

    @Override
    public T getResource(int slot) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].getResource(getStorageSlot(slot, storageIndex));
    }

    @Override
    public long getAmount(int slot) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].getAmount(getStorageSlot(slot, storageIndex));
    }

    @Override
    public long getCapacity(int slot, T resource) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].getCapacity(getStorageSlot(slot, storageIndex), resource);
    }

    @Override
    public boolean isValid(int slot, T resource) {
        int storageIndex = getStorageIndex(slot);
        return storages[storageIndex].isValid(getStorageSlot(slot, storageIndex), resource);
    }
}
