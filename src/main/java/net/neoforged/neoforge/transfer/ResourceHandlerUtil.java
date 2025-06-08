/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nonnegative;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
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
        return amount <= 0 || resource.isEmpty();
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
        var size = handler.size();
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
        var size = handler.size();
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
        var size = handler.size();
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
        return Mth.lerpDiscrete(proportion, 0, 15);
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
    @Nonnegative
    public static <T extends IResource> int insertStacking(
            IResourceHandler<T> handler,
            T resource,
            @Nonnegative int amount,
            @Nullable TransactionContext transaction) {
        try (var tx = Transaction.open(transaction)) {
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
            @Nonnegative int amount,
            @Nullable TransactionContext transaction) {
        try (var subTransaction = Transaction.open(transaction)) {
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
    public static <T extends IResource> int extract(IResourceHandler<T> handler, T resource, @Nonnegative int amount, @Nullable TransactionContext transaction) {
        try (var subTransaction = Transaction.open(transaction)) {
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
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
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
            @Nonnegative int amount,
            R defaultResource,
            @Nullable TransactionContext transaction,
            IStackFactory<R, S> stackFactory) {
        try (var subTransaction = Transaction.open(transaction)) {
            int size = handler.size();
            var handled = 0;
            R resourceTarget = defaultResource;
            for (int index = 0; index < size; index++) {
                R resource = handler.getResource(index);
                if (resource.isEmpty()) continue;
                if (!filter.test(resource)) continue;
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
            @Nonnegative int index,
            @Nonnegative int amount,
            R defaultResource,
            @Nullable TransactionContext transaction,
            IStackFactory<R, S> stackFactory) {
        try (var subTransaction = Transaction.open(transaction)) {
            R resource = handler.getResource(index);
            if (!filter.test(resource))
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
     * <p>Here is a usage example with fluid variant storages:
     *
     * <pre>{@code
     * // Source
     * IResourceHandler<FluidResource> source;
     * // Target
     * IResourceHandler<FluidResource> target;
     * Predicate<FluidResource> filter = resource -> resource.is(Fluids.WATER);
     *
     * // Move up to one bucket in total from source to target, outside of a transaction:
     * int amountMoved = ResourceHandlerUtil.move(source, target, FluidType.BUCKET, null);
     * // Move exactly one bucket in total, only of water:
     * try (Transaction transaction = Transaction.openOuter()) {
     *     int waterMoved = ResourceHandlerUtil.move(source, target, filter, FluidType.BUCKET, transaction);
     *     if (waterMoved == FluidType.BUCKET) {
     *         // Only commit if exactly one bucket was moved (no less!).
     *         transaction.commit();
     *     }
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
            @Nonnegative int amount,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (from == null || to == null) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                var fromResource = from.getResource(index);
                if (fromResource.isEmpty() || !filter.test(fromResource)) continue;

                // check how much can be extracted
                int maxExtracted;
                try (var simulatedExtract = Transaction.open(subTransaction)) {
                    maxExtracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtract);
                }

                try (Transaction transferTransaction = Transaction.open(subTransaction)) {
                    // check how much can be inserted
                    var inserted = to.insert(fromResource, maxExtracted, transferTransaction);

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
            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
            //noinspection DataFlowIssue
            report.addCategory("Move details")
                    .setDetail("Input storage", from::toString)
                    .setDetail("Output storage", to::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Max amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    /**
     * Similar to {@link #move}, but instead of all resources, it will be the first one that matches the filter. While this won't be a full list, this will simplify things like {@link FluidUtil#moveFluidWithSound}
     */
    public static <R extends IResource, S> S moveFirstOrDefault(
            @Nullable IResourceHandler<R> source,
            @Nullable IResourceHandler<R> destination,
            Predicate<R> filter,
            @Nonnegative int amount,
            R defaultResource,
            @Nullable TransactionContext transaction,
            IStackFactory<R, S> stackFactory) {
        Objects.requireNonNull(filter, "Filter may not be null");

        if (source == null || destination == null)
            return stackFactory.create(defaultResource, 0);

        try {
            int totalMoved = 0;
            R lastMovedResource = defaultResource;

            int size = source.size();

            for (int index = 0; index < size; ++index) {
                R fromResource = source.getResource(index);
                if (fromResource.isEmpty() || !filter.test(fromResource)) continue;

                // check how much can be extracted
                int extracted;
                try (var simulatedExtractTransaction = Transaction.open(transaction)) {
                    extracted = source.extract(index, fromResource, amount - totalMoved, simulatedExtractTransaction);
                }

                try (Transaction transferTransaction = Transaction.open(transaction)) {
                    // check how much can be inserted
                    var inserted = destination.insert(fromResource, extracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (source.extract(index, fromResource, inserted, transferTransaction) == inserted) {
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
                    .setDetail("Input storage", source::toString)
                    .setDetail("Output storage", destination::toString)
                    .setDetail("Filter", filter::toString)
                    .setDetail("Max amount", amount)
                    .setDetail("Transaction", transaction);
            throw new ReportedException(report);
        }
    }

    public static <T extends IResource> long getAmountAsLong(IResourceHandler<T> handler) {
        var sum = 0L;
        var size = handler.size();
        for (var index = 0; index < size; index++) {
            sum += handler.getAmountAsLong(index);
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    public static <T extends IResource> long getCapacityAsLong(IResourceHandler<T> handler) {
        var sum = 0L;
        var size = handler.size();
        for (var index = 0; index < size; index++) {
            sum += handler.getCapacityAsLong(index, handler.getResource(index));
            if (sum < 0) return Long.MAX_VALUE;
        }
        return sum;
    }

    /**
     * Moves a resource from one {@link IResourceHandler} to another.
     *
     * @param from        The source handler. May be null.
     * @param to          The target handler. May be null.
     *                    Only resources for which this filter returns {@code true} will be transferred.
     *                    This filter will never be tested with an empty resource, and filters are encouraged to throw an
     *                    exception if this guarantee is violated.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction this transfer is part of, or {@code null} if a transaction should be opened just for this transfer.
     * @param <T>         The type of resources to move.
     * @return The total amount of resources that was successfully transferred.
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    public static <T extends IResource> int move(IResourceHandler<T> from, IResourceHandler<T> to, @Nonnegative int amount, @Nullable TransactionContext transaction) {
        return move(from, to, Predicate.not(IResource::isEmpty), amount, transaction);
    }

    /**
     * @return {@code true} if the given resource is in the resource handler (though not necessarily interactable), {@code false} otherwise
     */
    public static <T extends IResource> boolean contains(IResourceHandler<T> handler, @Nonnegative T resource) {
        var size = handler.size();
        for (var index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index)))
                return true;
        }
        return false;
    }

    public static <T extends IResource> int indexOf(IResourceHandler<T> handler, @Nonnegative T resource) {
        var size = handler.size();
        for (var index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index)))
                return index;
        }
        return -1;
    }

    public static <T extends IResource> boolean hasExtractableResource(IResourceHandler<T> handler, T resource) {
        try (var temp = Transaction.open(null)) {
            //Simulated, we don't commit on an inquiry
            return handler.extract(resource, 1, temp) > 0;
        }
    }

    @FunctionalInterface
    public interface IStackFactory<R extends IResource, S> {
        S create(R resource, int amount);
    }

    private ResourceHandlerUtil() {}
}
