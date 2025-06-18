/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;

public final class ResourceHandlerUtil {
    /**
     * A utility method to check both resource and amount to validate if the resource would be empty.
     * <p>
     * Typically used in handler insert or extract implementations to determine if the operation is valid before proceeding.
     *
     * @see net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler#insert(IResource, int, TransactionContext) ResourceStorageHandler.insert(IResource, int, TransactionContext)
     */
    public static <T extends IResource> boolean isEmpty(T resource, int amount) {
        if (amount < 0) {
            CrashReport report = CrashReport.forThrowable(new IllegalArgumentException("Amount must be non-negative"), "Resource amount was negative");
            report.addCategory("ResourceHandlerUtil#isEmpty")
                    .setDetail("Resource", resource)
                    .setDetail("Amount", amount);
            throw new ReportedException(report);
        }
        return amount == 0 || resource.isEmpty();
    }

    /**
     * Checks if an {@link IResourceHandler} is empty.
     *
     * <p>An {@link IResourceHandler} is considered empty if all of its indices
     * contain either an empty resource or have an amount less than or equal to zero.
     *
     * @param handler the {@link IResourceHandler} to check for emptiness
     * @return {@code true} if the {@link IResourceHandler} is empty, {@code false} otherwise
     */
    public static boolean isEmpty(IResourceHandler<? extends IResource> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (!isIndexEmpty(handler, i))
                return false;
        }
        return true;
    }

    /**
     * Checks if an {@link IResourceHandler} is full.
     *
     * <p>An {@code IResourceHandler} is considered full if all of its indices contain resources with amounts
     * greater than or equal to their respective limits.
     *
     * @param handler the {@link IResourceHandler} to check
     * @return {@code true} if the {@link IResourceHandler} is full, {@code false} otherwise
     */
    public static boolean isFull(IResourceHandler<? extends IResource> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (!isIndexFull(handler, i))
                return false;
        }
        return true;
    }

    /**
     * Checks if a specific index of an {@link IResourceHandler} is empty.
     *
     * <p>An index is considered empty if the resource at the specified index is either empty or
     * the amount of the resource is less than or equal to zero.
     *
     * @param handler the {@link IResourceHandler} to check
     * @param index   the index of the resource to check
     * @return {@code true} if the resource at the specified index is empty, {@code false} otherwise
     */
    public static boolean isIndexEmpty(IResourceHandler<? extends IResource> handler, int index) {
        return handler.getAmount(index) == 0 || handler.getResource(index).isEmpty();
    }

    /**
     * Checks if a specific index of an {@link IResourceHandler} is full.
     *
     * <p>An index is considered full if the amount of the resource at the specified index is greater than or equal to
     * the limit of the resource at the specified index.
     *
     * @param handler the {@link IResourceHandler} to check
     * @param index   the index of the resource to check
     * @return {@code true} if the resource at the specified index is full, {@code false} otherwise
     */
    public static <T extends IResource> boolean isIndexFull(IResourceHandler<T> handler, int index) {
        //should we use the long or the "normal" returns for these?
        return handler.getAmountAsLong(index) >= handler.getCapacityAsLong(index, handler.getResource(index));
    }

    public static <T extends IResource> boolean resourceAndCountMatches(IResourceHandler<T> handler, int index, T resource, int amount) {
        return resourceMatches(handler, index, resource) && handler.getAmount(index) == amount;
    }

    public static <T extends IResource> boolean resourceMatches(IResourceHandler<T> handler, int index, T resource) {
        return handler.getResource(index).equals(resource);
    }

    public static <T extends IResource> boolean isValid(IResourceHandler<T> handler, T resource) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.isValid(i, resource))
                return true;
        }
        return false;
    }

    /**
     * Calculates the redstone signal strength based on the given resource handler. This value is between 0 and 15.
     * This method is based off of {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}
     *
     * @param handler the resource handler to calculate the signal from
     * @param <T>     the type of resource handled by the handler
     * @return the redstone signal strength
     */
    public static <T extends IResource> int getRedstoneSignalStrength(IResourceHandler<T> handler) {
        float proportion = 0.0F;
        int size = handler.size();

        for (int index = 0; index < size; ++index) {
            int indexFill = handler.getAmount(index);
            if (indexFill > 0)
                proportion += (float) indexFill / handler.getCapacity(index, handler.getResource(index));
        }

        proportion /= size;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /**
     * Inserts a resource into an {@link IResourceHandler} using stacking logic.
     * Resources will be inserted into filled slot(s) first, then empty slot(s).
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to insert the resource into
     * @param resource    the resource to insert
     * @param amount      the desired amount of the resource to insert
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the resource that was (or would have been, if simulated) inserted
     */
    public static <T extends IResource> int insertStacking(
            IResourceHandler<T> handler,
            T resource,
            int amount,
            @Nullable TransactionContext transaction) {
        try (Transaction tx = TransactionManager.open(transaction)) {
            int inserted = 0;
            int size = handler.size();
            for (int index = 0; index < size; index++) {
                if (ResourceHandlerUtil.isIndexEmpty(handler, index)) continue;
                inserted += handler.insert(index, resource, amount - inserted, tx);
                if (inserted >= amount)
                    return inserted;
            }

            for (int index = 0; index < size; index++) {
                if (!ResourceHandlerUtil.isIndexEmpty(handler, index)) continue;
                inserted += handler.insert(index, resource, amount - inserted, tx);
                if (inserted >= amount)
                    return inserted;
            }
            tx.commit();
            return inserted;
        }
    }

    /**
     * Inserts a resource into an {@link IResourceHandler} using non-stacking logic.
     * Resources will be inserted into the first slot(s) that can accept the resource.
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to insert the resource into
     * @param resource    the resource to insert
     * @param amount      the desired amount of the resource to insert
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the resource that was (or would have been, if simulated) inserted
     */
    public static <T extends IResource> int insertIndexForced(
            IResourceHandler<T> handler,
            T resource,
            int amount,
            @Nullable TransactionContext transaction) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            int inserted = 0;
            int size = handler.size();
            for (int index = 0; index < size; index++) {
                inserted += handler.insert(index, resource, amount - inserted, subTransaction);
                if (inserted >= amount)
                    return inserted;
            }
            subTransaction.commit();
            return inserted;
        }
    }

    /**
     * Extracts a resource from an {@link IResourceHandler}
     * Resources will be extracted from the first slot(s) that contain the resource.
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to extract the resource from
     * @param resource    the resource to extract
     * @param amount      the desired amount of the resource to extract
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> int extract(
            IResourceHandler<T> handler,
            T resource,
            int amount,
            @Nullable TransactionContext transaction) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            int extracted = 0;
            int size = handler.size();
            for (int index = 0; index < size; index++) {
                extracted += handler.extract(index, resource, amount - extracted, subTransaction);
                if (extracted >= amount)
                    return extracted;
            }
            subTransaction.commit();
            return extracted;
        }
    }

    public static <T extends IResource> int getTotalAmountOf(IResourceHandler<T> handler, T resource) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            //We don't commit allow us to just inquiry the amount
            return extract(handler, resource, Integer.MAX_VALUE, transaction);
        }
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param <R>          The type of resource handled by the handler
     * @param <S>          The type of stack created by the extraction
     * @param handler      The {@link IResourceHandler} to extract the resource from
     * @param filter       The filter to apply to the resources
     * @param amount       The desired amount of the resource to extract
     * @param transaction  The transaction context for a given insertion.
     *                     Passing in {@code null} will essentially be the same as doing `execute`,
     *                     whereas passing in a closeable context allows you to choose if it should be committed.
     * @param stackFactory A factory the given a resource of type {@code <R>} and an amount, a stack of type {@code <S>} can be created. The return is expected to be non-null and properly be the instanced empty value for a given resource.
     * @return a stack of type {@code <S>} typically in the form of an ResourceStack or as an example an ItemStack based on the factory provided
     */
    public static <R extends IResource, S> S extractFiltered(
            IResourceHandler<R> handler,
            Predicate<R> filter,
            int amount,
            R defaultResource,
            @Nullable TransactionContext transaction,
            IStackFactory<R, S> stackFactory) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            int size = handler.size();
            int handled = 0;
            R resourceTarget = defaultResource;
            for (int index = 0; index < size; index++) {
                R resource = handler.getResource(index);
                if (doesNotMatch(filter, resource)) continue;
                if (resourceTarget.isEmpty())
                    resourceTarget = resource;
                else if (!resourceTarget.equals(resource)) continue;

                handled += handler.extract(resource, amount - handled, subTransaction);
                if (handled == amount) {
                    subTransaction.commit();
                    return stackFactory.create(resource, handled);
                }
            }

            subTransaction.commit();
            if (handled == 0) return stackFactory.create(defaultResource, 0);
            return stackFactory.create(resourceTarget, handled);
        }
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that is not empty.
     *
     * @param <R>          the type of resource handled by the handler.
     * @param <S>          The type of stack returned by the handler.
     * @param index        The index that is being checked in the handler.
     * @param handler      the {@link IResourceHandler} to extract the resource from.
     * @param amount       the desired amount of the resource to extract.
     * @param transaction  The transaction context for a given insertion.
     *                     Passing in {@code null} will essentially be the same as doing `execute`,
     *                     whereas passing in a closeable context allows you to choose if it should be committed.
     * @param stackFactory A factory the given a resource of type {@code <R>} and an amount, a stack of type {@code <S>} can be created. The return is expected to be non-null and properly be the instanced empty value for a given resource.
     * @return a stack of type {@code <S>} typically in the form of an ResourceStack or as an example an ItemStack based on the factory provided
     */
    public static <R extends IResource, S> S extractIndexFiltered(
            IResourceHandler<R> handler,
            Predicate<R> filter,
            int index,
            int amount,
            R defaultResource,
            @Nullable TransactionContext transaction,
            IStackFactory<R, S> stackFactory) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            R resource = handler.getResource(index);
            if (doesNotMatch(filter, resource))
                return stackFactory.create(defaultResource, 0);
            int extract = handler.extract(resource, amount, subTransaction);
            subTransaction.commit();
            if (extract == 0) return stackFactory.create(defaultResource, 0);
            return stackFactory.create(resource, extract);
        }
    }

    /**
     * Move resources between two storages, matching the passed filter, and return the amount that was successfully transferred.
     *
     * <p>Here is a usage example:
     *
     * <pre>{@code
     * // Source
     * IResourceHandler<FluidResource> source;
     * // Target
     * IResourceHandler<FluidResource> target;
     * Predicate<FluidResource> filter = resource -> resource.is(Fluids.WATER);
     *
     * // Move exactly one bucket in total, only of water:
     * try (Transaction transaction = Transaction.open(TransactionContext.ROOT)) {
     *     int waterMoved = ResourceHandlerUtil.move(source, target, filter, FluidType.BUCKET_VOLUME, transaction);
     *     if (waterMoved == FluidType.BUCKET_VOLUME) {
     *         // Only commit if exactly one bucket was moved.
     *         transaction.commit();
     *     }
     *     //If committed, leaving this try-block will keep all changes.
     * }
     * }</pre>
     *
     * @param from        The source handler. May be null.
     * @param to          The target handler. May be null.
     * @param filter      The filter for transferred resources.
     *                    Only resources for which this filter returns {@code true} will be transferred.
     *                    This filter will never be tested with an empty resource, and filters are encouraged to throw an
     *                    exception if this guarantee is violated.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @param <T>         The type of resources to move.
     * @return The total amount of resources that was successfully transferred. This number is not necessarily for one resource, as we only pass in a filter. It is intended to be used to determine a raw number of resources moved.
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    public static <T extends IResource> int move(
            @Nullable IResourceHandler<T> from,
            @Nullable IResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (from == null || to == null) return 0;

        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                T fromResource = from.getResource(index);
                if (doesNotMatch(filter, fromResource)) continue;

                // check how much can be extracted
                int maxExtracted;
                try (Transaction simulatedExtract = TransactionManager.open(subTransaction)) {
                    maxExtracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtract);
                }

                try (Transaction transferTransaction = TransactionManager.open(subTransaction)) {
                    // check how much can be inserted
                    int inserted = to.insert(fromResource, maxExtracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (from.extract(index, fromResource, inserted, transferTransaction) == inserted) {
                        totalMoved += inserted;
                        transferTransaction.commit();
                    }
                }

                if (amount == totalMoved) {
                    // early return if nothing can be moved anymore
                    subTransaction.commit();
                    return totalMoved;
                }
            }

            subTransaction.commit();
            return totalMoved;
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between resource handlers");
            //noinspection DataFlowIssue
            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    /**
     * Similar to {@link #move}, but instead of all resources, it will be the first one that matches the filter. While this won't be a full list, this will simplify things like {@link FluidUtil#moveFluidWithSound}
     */
    public static <R extends IResource, S> S moveFirstOrDefault(
            @Nullable IResourceHandler<R> from,
            @Nullable IResourceHandler<R> to,
            Predicate<R> filter,
            int amount,
            R defaultResource,
            @Nullable TransactionContext transaction,
            IStackFactory<R, S> stackFactory) {
        Objects.requireNonNull(filter, "Filter may not be null");

        if (from == null || to == null)
            return stackFactory.create(defaultResource, 0);

        try {
            int totalMoved = 0;
            R lastMovedResource = defaultResource;

            int size = from.size();

            for (int index = 0; index < size; ++index) {
                R fromResource = from.getResource(index);
                if (doesNotMatch(filter, fromResource)) continue;

                // check how much can be extracted
                int extracted;
                try (Transaction simulatedExtractTransaction = TransactionManager.open(transaction)) {
                    extracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtractTransaction);
                }

                try (Transaction transferTransaction = TransactionManager.open(transaction)) {
                    // check how much can be inserted
                    int inserted = to.insert(fromResource, extracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (from.extract(index, fromResource, inserted, transferTransaction) == inserted) {
                        totalMoved += inserted;
                        transferTransaction.commit();
                        lastMovedResource = fromResource;
                    }
                }

                if (amount == totalMoved) {
                    // early return if nothing can be moved anymore
                    return stackFactory.create(lastMovedResource, totalMoved);
                }
            }
            return stackFactory.create(totalMoved == 0 ? defaultResource : lastMovedResource, totalMoved);
        } catch (Exception e) {
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
            //noinspection DataFlowIssue
            report.addCategory("Move details")
                    .setDetail("Input", from::toString)
                    .setDetail("Output", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    public static <T extends IResource> long getAmountAsLong(IResourceHandler<T> handler) {
        long sum = 0L;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            sum += handler.getAmountAsLong(index);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    public static <T extends IResource> long getCapacityAsLong(IResourceHandler<T> handler) {
        long sum = 0L;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            sum += handler.getCapacityAsLong(index, handler.getResource(index));
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * @return {@code true} if the given resource is in the resource handler (though not necessarily interactable), {@code false} otherwise
     */
    public static <T extends IResource> boolean contains(IResourceHandler<T> handler, T resource) {
        return indexOf(handler, resource) != -1;
    }

    public static <T extends IResource> int indexOf(IResourceHandler<T> handler, T resource) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index)))
                return index;
        }
        return -1;
    }

    public static <T extends IResource> boolean hasExtractableResource(IResourceHandler<T> handler, Predicate<T> filter) {
        try (Transaction temp = TransactionManager.open(null)) {
            //Simulated, we don't commit on an inquiry
            var size = handler.size();
            for (int index = 0; index < size; index++) {
                var resource = handler.getResource(index);
                if (!doesNotMatch(filter, resource) && handler.extract(resource, 1, temp) > 0)
                    return true;
            }
            return false;
        }
    }

    /**
     * Empty never matches, and uses the filter to validate the resource.
     * 
     * @return {@code false} if the resource does match the filter. Empty is always {@code true}
     */
    private static <T extends IResource> boolean doesNotMatch(Predicate<T> filter, T resource) {
        return resource.isEmpty() || !filter.test(resource);
    }

    public static <T extends IResource> boolean hasExtractableResourceAtIndex(IResourceHandler<T> handler, Predicate<T> filter, int index) {
        try (Transaction temp = TransactionManager.open(null)) {
            //Simulated: we don't commit
            var resource = handler.getResource(index);
            return !doesNotMatch(filter, resource) && handler.extract(resource, 1, temp) > 0;
        }
    }

    public static <T extends IResource> boolean hasExtractableResource(IResourceHandler<T> handler, T resource) {
        try (Transaction temp = TransactionManager.open(null)) {
            //Simulated, we don't commit on an inquiry
            return handler.extract(resource, 1, temp) > 0;
        }
    }

    public static <T extends IResource> T getFirstResourceOrDefault(IResourceHandler<T> handler, T defaultResource) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            T resource = handler.getResource(index);
            if (!resource.isEmpty())
                return resource;
        }
        return defaultResource;
    }

    private ResourceHandlerUtil() {}
}
