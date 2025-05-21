package net.neoforged.neoforge.transfer.storage;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.transfer.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Helper functions to work with {@link Storage}s.
 *
 * <p>Note that the functions that take a predicate iterate over the entire inventory in the worst case.
 * If the resource is known, there will generally be a more performance efficient way.
 */
public final class StorageUtil {
    private StorageUtil() {
    }

    // TODO: example needs to be adjusted probably
    /**
     * Move resources between two storages, matching the passed filter, and return the amount that was successfully transferred.
     *
     * <p>Here is a usage example with fluid variant storages:
     * <pre>{@code
     * // Source
     * Storage<FluidVariant> source;
     * // Target
     * Storage<FluidVariant> target;
     *
     * // Move up to one bucket in total from source to target, outside of a transaction:
     * long amountMoved = StorageUtil.move(source, target, variant -> true, FluidConstants.BUCKET, null);
     * // Move exactly one bucket in total, only of water:
     * try (Transaction transaction = Transaction.openOuter()) {
     *     Predicate<FluidVariant> filter = variant -> variant.isOf(Fluids.WATER);
     *     long waterMoved = StorageUtil.move(source, target, filter, FluidConstants.BUCKET, transaction);
     *     if (waterMoved == FluidConstants.BUCKET) {
     *         // Only commit if exactly one bucket was moved (no less!).
     *         transaction.commit();
     *     }
     * }
     * }</pre>
     *
     * @param from The source storage. May be null.
     * @param to The target storage. May be null.
     * @param filter The filter for transferred resources.
     *               Only resources for which this filter returns {@code true} will be transferred.
     *               This filter will never be tested with a blank resource, and filters are encouraged to throw an
     *               exception if this guarantee is violated.
     * @param maxAmount The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @param <T> The type of resources to move.
     * @return The total amount of resources that was successfully transferred.
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    public static <T extends Resource> long move(@Nullable Storage<T> from, @Nullable Storage<T> to, Predicate<T> filter, long maxAmount, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (from == null || to == null) return 0;

        long totalMoved = 0;

        try (Transaction iterationTransaction = Transaction.openNested(transaction)) {
            for (int slot : from.nonEmptySlots()) {
                T resource = from.getResource(slot);
                if (!filter.test(resource)) continue;

                // check how much can be extracted
                long maxExtracted = simulateExtract(from, slot, resource, maxAmount - totalMoved, iterationTransaction);

                try (Transaction transferTransaction = iterationTransaction.openNested()) {
                    // check how much can be inserted
                    long accepted = to.insert(resource, maxExtracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (from.extract(slot, resource, accepted, transferTransaction) == accepted) {
                        totalMoved += accepted;
                        transferTransaction.commit();
                    }
                }

                if (maxAmount == totalMoved) {
                    // early return if nothing can be moved anymore
                    iterationTransaction.commit();
                    return totalMoved;
                }
            }

            iterationTransaction.commit();
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
            report.addCategory("Move details")
                    .setDetail("Input storage", from::toString)
                    .setDetail("Output storage", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Max amount", maxAmount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }

        return totalMoved;
    }

    /**
     * Convenient helper to simulate an insertion, i.e. get the result of insert without modifying any state.
     * The passed transaction may be null if a new transaction should be opened for the simulation.
     * @see Storage#insert
     */
    public static <T extends Resource> long simulateInsert(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
            return storage.insert(resource, maxAmount, simulateTransaction);
        }
    }

    /**
     * Convenient helper to simulate an insertion, i.e. get the result of insert without modifying any state.
     * The passed transaction may be null if a new transaction should be opened for the simulation.
     * @see Storage#insert
     */
    public static <T extends Resource> long simulateInsert(Storage<T> storage, int slot, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
            return storage.insert(slot, resource, maxAmount, simulateTransaction);
        }
    }

    /**
     * Convenient helper to simulate an extraction, i.e. get the result of extract without modifying any state.
     * The passed transaction may be null if a new transaction should be opened for the simulation.
     * @see Storage#insert
     */
    public static <T extends Resource> long simulateExtract(Storage<T> storage, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
            return storage.extract(resource, maxAmount, simulateTransaction);
        }
    }

    /**
     * Convenient helper to simulate an extraction, i.e. get the result of extract without modifying any state.
     * The passed transaction may be null if a new transaction should be opened for the simulation.
     * @see Storage#insert
     */
    public static <T extends Resource> long simulateExtract(Storage<T> storage, int slot, T resource, long maxAmount, @Nullable TransactionContext transaction) {
        try (Transaction simulateTransaction = Transaction.openNested(transaction)) {
            return storage.extract(slot, resource, maxAmount, simulateTransaction);
        }
    }

    /**
     * Try to extract any resource from a storage, up to a maximum amount.
     *
     * <p>This function will only ever pull from one storage view of the storage, even if multiple storage views contain the same resource.
     *
     * @param storage The storage, may be null.
     * @param maxAmount The maximum to extract.
     * @param transaction The transaction this operation is part of.
     * @return A non-blank resource and the strictly positive amount of it that was extracted from the storage,
     * or {@code null} if none could be found.
     */
    @Nullable
    public static <T extends Resource> ResourceAmount<T> extractAny(@Nullable Storage<T> storage, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);

        if (storage == null) return null;

        try {
            for (int slot : storage.nonEmptySlots()) {
                T resource = storage.getResource(slot);
                long amount = storage.extract(slot, resource, maxAmount, transaction);
                if (amount > 0) return new ResourceAmount<>(resource, amount);
            }
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Extracting resources from storage");
            report.addCategory("Extraction details")
                    .setDetail("Storage", storage::toString)
                    .setDetail("Max amount", maxAmount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }

        return null;
    }

    /**
     * Try to insert up to some amount of a resource into a list of storage slots, trying to "stack" first,
     * i.e. prioritizing slots that already contain the resource.
     *
     * @return How much was inserted.
     * @see Storage#insert
     */
    public static <T extends Resource> long insertStacking(@Nullable Storage<T> storage, T resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notNegative(maxAmount);
        if (storage == null) return 0;

        long amount = 0;

        try {
            int size = storage.size();

            for (int i = 0; i < size; ++i) {
                if (!storage.getResource(i).isBlank()) {
                    amount += storage.insert(i, resource, maxAmount - amount, transaction);
                    if (amount == maxAmount) return amount;
                }
            }

            for (int i = 0; i < size; ++i) {
                amount += storage.insert(resource, maxAmount - amount, transaction);
                if (amount == maxAmount) return amount;
            }
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Inserting resources into slots");
            report.addCategory("Insertion details")
                    .setDetail("Storage", storage::toString)
                    .setDetail("Resource", () -> Objects.toString(resource, null))
                    .setDetail("Max amount", maxAmount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }

        return amount;
    }

    /**
     * Attempt to find a resource stored in the passed storage.
     *
     * @see #findStoredResource(Storage, Predicate)
     * @return A non-blank resource stored in the storage, or {@code null} if none could be found.
     */
    @Nullable
    public static <T extends Resource> T findStoredResource(@Nullable Storage<T> storage) {
        return findStoredResource(storage, r -> true);
    }

    /**
     * Attempt to find a resource stored in the passed storage that matches the passed filter.
     *
     * @param storage The storage to inspect, may be null.
     * @param filter The filter. Only a resource for which this filter returns {@code true} will be returned.
     * @param <T> The type of the stored resources.
     * @return A non-blank resource stored in the storage that matches the filter, or {@code null} if none could be found.
     */
    @Nullable
    public static <T extends Resource> T findStoredResource(@Nullable Storage<T> storage, Predicate<T> filter) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (storage == null) return null;

        for (int slot : storage.nonEmptySlots()) {
            T resource = storage.getResource(slot);
            if (filter.test(resource)) {
                return resource;
            }
        }

        return null;
    }

    /**
     * Attempt to find a resource stored in the passed storage that can be extracted.
     *
     * @see #findExtractableResource(Storage, Predicate, TransactionContext)
     * @return A non-blank resource stored in the storage that can be extracted, or {@code null} if none could be found.
     */
    @Nullable
    public static <T extends Resource> T findExtractableResource(@Nullable Storage<T> storage, @Nullable TransactionContext transaction) {
        return findExtractableResource(storage, r -> true, transaction);
    }

    /**
     * Attempt to find a resource stored in the passed storage that matches the passed filter and can be extracted.
     *
     * @param storage The storage to inspect, may be null.
     * @param filter The filter. Only a resource for which this filter returns {@code true} will be returned.
     * @param transaction The current transaction, or {@code null} if a transaction should be opened for this query.
     * @param <T> The type of the stored resources.
     * @return A non-blank resource stored in the storage that matches the filter and can be extracted, or {@code null} if none could be found.
     */
    @Nullable
    @Contract("null,_,_-> null")
    public static <T extends Resource> T findExtractableResource(@Nullable Storage<T> storage, Predicate<T> filter, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (storage == null) return null;

        try (Transaction nested = Transaction.openNested(transaction)) {
            for (int slot : storage.nonEmptySlots()) {
                // Extract below could change the resource, so we have to query it before extracting.
                T resource = storage.getResource(slot);

                if (filter.test(resource) && storage.extract(slot, resource, Long.MAX_VALUE, nested) > 0) {
                    // Will abort the extraction.
                    return resource;
                }
            }
        }

        return null;
    }

    /**
     * Attempt to find a resource stored in the passed storage that can be extracted, and how much of it can be extracted.
     *
     * @see #findExtractableContent(Storage, Predicate, TransactionContext)
     * @return A non-blank resource stored in the storage that can be extracted, and the strictly positive amount of it that can be extracted,
     * or {@code null} if none could be found.
     */
    @Nullable
    public static <T extends Resource> ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, @Nullable TransactionContext transaction) {
        return findExtractableContent(storage, r -> true, transaction);
    }

    /**
     * Attempt to find a resource stored in the passed storage that can be extracted and matches the filter, and how much of it can be extracted.
     *
     * @param storage The storage to inspect, may be null.
     * @param filter The filter. Only a resource for which this filter returns {@code true} will be returned.
     * @param transaction The current transaction, or {@code null} if a transaction should be opened for this query.
     * @param <T> The type of the stored resources.
     * @return A non-blank resource stored in the storage that can be extracted and matches the filter, and the strictly positive amount of it that can be extracted,
     * or {@code null} if none could be found.
     */
    @Nullable
    public static <T extends Resource> ResourceAmount<T> findExtractableContent(@Nullable Storage<T> storage, Predicate<T> filter, @Nullable TransactionContext transaction) {
        T extractableResource = findExtractableResource(storage, filter, transaction);

        if (extractableResource != null) {
            long extractableAmount = simulateExtract(storage, extractableResource, Long.MAX_VALUE, transaction);

            if (extractableAmount > 0) {
                return new ResourceAmount<>(extractableResource, extractableAmount);
            }
        }

        return null;
    }

    /**
     * Compute the redstone signal, i.e. comparator output for a storage,
     * similar to {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}.
     *
     * @param storage The storage for which the comparator level should be computed.
     * @param <T> The type of the stored resources.
     * @return An integer between 0 and 15 (inclusive): the comparator output for the passed storage.
     */
    public static <T extends Resource> int getRedstoneSignalFromStorage(@Nullable Storage<T> storage) {
        if (storage == null) return 0;

        double fillPercentage = 0;
        int slots = storage.size();
        boolean hasNonEmptySlot = false;

        for (int i = 0; i < slots; ++i) {
            long amount = storage.getAmount(i);

            if (amount > 0) {
                fillPercentage += (double) amount / storage.getCapacity(i, storage.getResource(i));
                hasNonEmptySlot = true;
            }
        }

        return Mth.floor(fillPercentage / slots * 14) + (hasNonEmptySlot ? 1 : 0);
    }
}
