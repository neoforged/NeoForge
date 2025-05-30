package net.neoforged.neoforge.transfer.fluid;

import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A context object that is used when a {@link Storage} is
 * located inside an item (or potentially multiple, when it is stacked).
 */
public interface InItemStorageContext {
    /**
     * @return The current variant of the item that contains the storage that should be accessed. This may
     * be blank or become blank if {@link #extract} is used.
     */
    ItemVariant getCurrent();

    /**
     * @return The current amount of {@linkplain #getCurrent() the item} containing the storage.
     */
    long getCurrentAmount();

    /**
     * Transactionally insert an item into the "slot" representing the location of the item containing the storage.
     * <p>
     * If the inserted item is not stackable with the current item, it may be inserted in a place that is inaccessible
     * for {@link #extract}, such as the player inventory.
     *
     * @see Storage#insert
     */
    long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction);

    /**
     * Transactionally extract some of the current item.
     *
     * @see Storage#extract
     */
    long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction);

    /**
     * Try to exchange as many items as possible of {@linkplain #getCurrent() the current item} with another.
     * <p>
     * That is, {@link #extract} the given amount of the current item, and transactionally {@link #insert} the same amount of the given variant instead.
     * <p>
     * This method is particularly useful if the contained storage is being mutated and the resulting changes to the item
     * should be persisted.
     *
     * @param newVariant  The variant of the items after the conversion. May not be blank.
     * @param maxAmount   The maximum amount of items to convert. May not be negative.
     * @param transaction The transaction this operation is part of.
     * @return A non-negative integer not greater than maxAmount: the amount that was transformed.
     */
    default long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);

        try (var nested = Transaction.open(transaction)) {
            long extracted = extract(getCurrent(), maxAmount, nested);

            if (insert(newVariant, extracted, nested) == extracted) {
                nested.commit();
                return extracted;
            }
        }

        return 0;
    }
}
