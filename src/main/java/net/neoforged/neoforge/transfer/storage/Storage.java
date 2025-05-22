package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A generic handler for handling a resource of type {@link T}.
 * @param <T> The type of resource this handler manages.
 */
public interface Storage<T> {
    /**
     * @return The number of indices this handler manages.
     */
    int size();

    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index The index to insert the resource into.
     * @param resource The resource to insert.
     * @param maxAmount The amount of the resource to insert.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    long insert(int index, T resource, long maxAmount, TransactionContext transaction);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     *
     * @param resource The resource to insert.
     * @param maxAmount The amount of the resource to insert.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    default long insert(T resource, long maxAmount, TransactionContext transaction) {
        long amount = 0;
        int slots = size();

        for (int i = 0; i < slots; ++i) {
            amount += insert(i, resource, maxAmount - amount, transaction);
            if (amount == maxAmount) break;
        }

        return amount;
    }

    /**
     * Return false if calling {@link #insert} will absolutely always return 0, or true otherwise or in doubt.
     *
     * <p>Note: This function is meant to be used by pipes or other devices that can transfer resources to know if
     * they should interact with this storage at all.
     */
    default boolean supportsInsertion() {
        return true;
    }

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index The index to extract the resource from.
     * @param resource The resource to extract.
     * @param maxAmount The amount of the resource to extract.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    long extract(int index, T resource, long maxAmount, TransactionContext transaction);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     *
     * @param resource The resource to extract.
     * @param maxAmount The amount of the resource to extract.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    default long extract(T resource, long maxAmount, TransactionContext transaction) {
        long amount = 0;
        int slots = size();

        for (int i = 0; i < slots; ++i) {
            amount += extract(i, resource, maxAmount - amount, transaction);
            if (amount == maxAmount) break;
        }

        return amount;
    }

    /**
     * Return false if calling {@link #extract} will absolutely always return 0, or true otherwise or in doubt.
     *
     * <p>Note: This function is meant to be used by pipes or other devices that can transfer resources to know if
     * they should interact with this storage at all.
     */
    default boolean supportsExtraction() {
        return true;
    }

    /**
     * Return {@code true} if the contained {@link #getResource} is blank, or {@code false} otherwise.
     *
     * <p>This function is mostly useful when dealing with storages of arbitrary types.
     * For transfer variant storages, this should always be equivalent to {@code getResource(index).isBlank()}.
     */
    boolean isResourceBlank(int index);

    /**
     * @param index The index to get the resource from.
     * @return The resource at the given index.
     */
    // TODO: rename index to slot?
    T getResource(int index);

    /**
     * @param index The index to get the amount from.
     * @return The amount of the resource at the given index.
     */
    long getAmount(int index);

    /**
     * Gets the maximum amount that the given index can have of the given resource.
     *
     * @param index The index to get the limit from.
     * @param resource The resource to get the limit for. Might be blank to request a "generic" limit.
     * @return The limit of the resource at the given index.
     */
    long getCapacity(int index, T resource);

    /**
     * Checks if the given resource is generally allowed to be inserted into the handler at the given index,
     * regardless of the current state of the handler.
     *
     * @param index The index to check.
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean isValid(int index, T resource);

    /**
     * Return a class instance of this interface with the desired generic type,
     * to be used for easier registration with capabilities.
     */
    static <T> Class<Storage<T>> asClass() {
        return (Class<Storage<T>>) (Object) Storage.class;
    }
}
