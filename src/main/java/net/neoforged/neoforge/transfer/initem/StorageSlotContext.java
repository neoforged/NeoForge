package net.neoforged.neoforge.transfer.initem;

import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class StorageSlotContext implements InItemStorageContext {
    private final Storage<ItemVariant> storage;
    private final int slot;

    StorageSlotContext(Storage<ItemVariant> storage, int slot) {
        this.storage = storage;
        this.slot = slot;
    }

    @Override
    public ItemVariant getCurrent() {
        return storage.getResource(slot);
    }

    @Override
    public long getCurrentAmount() {
        return storage.getAmount(slot);
    }

    @Override
    public boolean supportsModification() {
        return storage.supportsInsertion() || storage.supportsExtraction();
    }

    @Override
    public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        long inserted = storage.insert(slot, itemVariant, maxAmount, transaction);
        if (inserted < maxAmount) {
            inserted += storage.insert(itemVariant, maxAmount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return storage.extract(slot, itemVariant, maxAmount, transaction);
    }

    @Override
    public String toString() {
        return "StorageSlotContext{" +
                "storage=" + storage +
                ", slot=" + slot +
                '}';
    }
}
