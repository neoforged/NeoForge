package net.neoforged.neoforge.transfer.initem;

import net.neoforged.neoforge.transfer.item.InventoryStorage;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class CreativePlayerStorageContext implements InItemStorageContext {
    private final InventoryStorage inventoryStorage;
    private final ItemVariant variant;
    private final long amount;

    CreativePlayerStorageContext(InventoryStorage inventoryStorage, ItemVariant variant, long amount) {
        this.inventoryStorage = inventoryStorage;
        this.variant = variant;
        this.amount = amount;
    }

    @Override
    public ItemVariant getCurrent() {
        return variant;
    }

    @Override
    public long getCurrentAmount() {
        return amount;
    }

    @Override
    public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(itemVariant, maxAmount);

        if (maxAmount > 0) {
            // Only add the item to the player inventory if it's not already in the inventory.
            boolean hasItem = false;

            // TODO: this is a good case for a "contains" helper method
            int size = inventoryStorage.size();
            for (int i = 0; i < size; ++i) {
                if (inventoryStorage.getResource(i).equals(itemVariant) && inventoryStorage.getAmount(i) > 0) {
                    hasItem = true;
                    break;
                }
            }

            if (!hasItem) {
                inventoryStorage.insert(itemVariant, 1, transaction);
            }
        }

        // Insertion always succeeds from the POV of the context user.
        return maxAmount;
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(itemVariant, maxAmount);
        // Pretend we can extract anything, but never actually do it.
        return maxAmount;
    }
}
