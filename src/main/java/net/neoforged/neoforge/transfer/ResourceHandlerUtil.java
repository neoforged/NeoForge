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
import org.jetbrains.annotations.Nullable;

public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {}

    /**
     * Determines if either the given resource or amount is classified as empty: if either {@link IResource#isEmpty()} is {@code true},
     * or the amount is zero (or negative) then the resource is considered empty.
     *
     * @param resource The resource to check.
     * @param amount   An amount to check.
     * @return {@code true} if either {@link IResource#isEmpty()} returns {@code true}, or the amount is {@code <= 0}.
     */
    public static boolean isEmpty(IResource resource, int amount) {
        return amount <= 0 || resource.isEmpty();
    }

    /**
     * Checks if an {@link IResourceHandler} is empty.
     *
     * <p>An {@link IResourceHandler} is considered empty if all of its indices
     * contain either an empty resource or have an amount less than or equal to zero.
     * A handler with zero indices will always return true.
     *
     * @param handler the {@link IResourceHandler} to check for emptiness
     * @return {@code true} if the {@link IResourceHandler} is empty, {@code false} otherwise
     */
    public static boolean isEmpty(IResourceHandler<? extends IResource> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmount(i) > 0 || !handler.getResource(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an {@link IResourceHandler} is full.
     * <p>
     * An {@code IResourceHandler} is considered full if all of its indices contain resources with amounts
     * greater than or equal to their respective limits.
     * Note, A handler with zero indices will always return that it is full.
     *
     * @param handler the {@link IResourceHandler} to check
     * @return {@code true} if the {@link IResourceHandler} is full, {@code false} otherwise
     */
    public static <T extends IResource> boolean isFull(IResourceHandler<T> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmount(i) < handler.getCapacity(i, handler.getResource(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the given resource {@link IResourceHandler#isValid is valid} in any index of the given resource handler.
     *
     * @param handler  the {@link IResourceHandler} to check
     * @param resource the resource to check
     * @return {@code true} if the resource is valid in any index of the handler.
     * @see IResourceHandler#isValid(int, IResource)
     */
    public static <T extends IResource> boolean isValid(IResourceHandler<T> handler, T resource) {
        TransferPreconditions.checkNonEmpty(resource);

        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.isValid(i, resource)) {
                return true;
            }
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
    public static <T extends IResource> int getRedstoneSignalFromResourceHandler(IResourceHandler<T> handler) {
        float proportion = 0.0F;
        int size = handler.size();

        for (int index = 0; index < size; ++index) {
            long indexFill = handler.getAmount(index);
            if (indexFill > 0) {
                proportion += (float) indexFill / handler.getCapacity(index, handler.getResource(index));
            }
        }

        proportion /= size;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /**
     * Tries to insert up to some amount of a resource into the handler,
     * using stacking logic: resources will be inserted into filled indices first, then empty indices.
     *
     * @param handler     the {@link IResourceHandler} to insert the resource into
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     *                    Passing in {@code null} will open a root transaction, and commit it at the end of the method.
     * @return the amount of the resource that was inserted
     */
    public static <T extends IResource> int insertStacking(
            IResourceHandler<T> handler,
            T resource,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) return 0;

        try (Transaction tx = Transaction.open(transaction)) {
            int inserted = 0;
            int size = handler.size();
            // Attempt to insert into indices with a non-empty resource first
            for (int index = 0; index < size; index++) {
                if (!handler.getResource(index).isEmpty()) {
                    inserted += handler.insert(index, resource, amount - inserted, tx);
                    if (inserted == amount) break;
                }
            }

            // Then go through empty indices
            for (int index = 0; index < size; index++) {
                if (handler.getResource(index).isEmpty()) {
                    inserted += handler.insert(index, resource, amount - inserted, tx);
                    if (inserted == amount) break;
                }
            }

            tx.commit();
            return inserted;
        }
    }

    // TODO: stack factory usage
//    /**
//     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
//     *
//     * @param <R>          The type of resource handled by the handler
//     * @param <S>          The type of stack created by the extraction
//     * @param handler      The {@link IResourceHandler} to extract the resource from
//     * @param filter       The filter to apply to the resources
//     * @param amount       The desired amount of the resource to extract
//     * @param transaction  The transaction context for the operation.
//     *                     Passing in {@code null} will open a root transaction, whereas passing in a transaction will
//     *                     allow you to make the final decision to commit based on the results of this method.
//     * @param stackFactory A factory the given a resource of type {@code <R>} and an amount, a stack of type {@code <S>} can be created. The return is expected to be non-null and properly be the instanced empty value for a given resource.
//     * @return a stack of type {@code <S>} typically in the form of an ResourceStack or as an example an ItemStack based on the factory provided
//     */
//    public static <R extends IResource, S> S extract(
//            IResourceHandler<R> handler,
//            Predicate<R> filter,
//            int amount,
//            R defaultResource,
//            @Nullable TransactionContext transaction,
//            IStackFactory<R, S> stackFactory) {
//        TransferPreconditions.checkNonNegative(amount);
//        if (amount == 0) return stackFactory.create(defaultResource, 0);
//
//        int size = handler.size();
//        int handled = 0;
//        R resourceTarget = defaultResource;
//
//        try (Transaction subTransaction = Transaction.open(transaction)) {
//            for (int index = 0; index < size; index++) {
//                R resource = handler.getResource(index);
//                //Filter testing
//                if (doesNotMatch(filter, resource))
//                    continue;
//
//                if (resourceTarget.isEmpty()) { //If our current resource that we are expecting to return is still empty
//                    resourceTarget = resource;
//                } else if (!resourceTarget.equals(resource)) { // If it isn't empty, we check if it matches the one we found
//                    continue;
//                }
//
//                handled += handler.extract(resource, amount - handled, subTransaction);
//                if (handled == amount) break;
//            }
//
//            subTransaction.commit();
//            if (handled == 0) return stackFactory.create(defaultResource, 0);
//            return stackFactory.create(resourceTarget, handled);
//        }
//    }
//
//    /**
//     * Extracts the first resource from an {@link IResourceHandler} that is not empty.
//     *
//     * @param <R>          the type of resource handled by the handler.
//     * @param <S>          The type of stack returned by the handler.
//     * @param handler      the {@link IResourceHandler} to extract the resource from.
//     * @param filter       The filter to apply to the resources
//     * @param index        The index that is being checked in the handler.
//     * @param amount       the desired amount of the resource to extract.
//     * @param transaction  The transaction context for the operation.
//     *                     Passing in {@code null} will open a root transaction, whereas passing in a transaction will
//     *                     allow you to make the final decision to commit based on the results of this method.
//     * @param stackFactory A factory the given a resource of type {@code <R>} and an amount, a stack of type {@code <S>} can be created. The return is expected to be non-null and properly be the instanced empty value for a given resource.
//     * @return a stack of type {@code <S>} typically in the form of an ResourceStack or as an example an ItemStack based on the factory provided
//     */
//    public static <R extends IResource, S> S extract(
//            IResourceHandler<R> handler,
//            Predicate<R> filter,
//            int index,
//            int amount,
//            R defaultResource,
//            @Nullable TransactionContext transaction,
//            IStackFactory<R, S> stackFactory) {
//        TransferPreconditions.checkNonNegative(amount);
//        if (amount == 0) return stackFactory.create(defaultResource, 0);
//
//        R resource = handler.getResource(index);
//        if (doesNotMatch(filter, resource))
//            return stackFactory.create(defaultResource, 0);
//        try (Transaction subTransaction = Transaction.open(transaction)) {
//            int extract = handler.extract(resource, amount, subTransaction);
//            if (extract == 0)
//                return stackFactory.create(defaultResource, 0);
//            subTransaction.commit();
//            return stackFactory.create(resource, extract);
//        }
//    }

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
     *
     * // Move exactly one bucket in total, only of water:
     * try (Transaction transaction = Transaction.open(null)) {
     *     int waterMoved = ResourceHandlerUtil.move(source, target, fr -> fr.is(Fluids.WATER), FluidType.BUCKET_VOLUME, transaction);
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
     *                    This filter will never be tested with an empty resource.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction that this operation is part of.
     *                    Passing in {@code null} will open a root transaction, and commit it at the end of the method.
     *                    Passing in a transaction will allow the caller to make the final decision to commit or not,
     *                    based on the results of this method.
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
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) return 0;
        if (from == null || to == null) return 0;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                T fromResource = from.getResource(index);
                if (fromResource.isEmpty() || !filter.test(fromResource)) continue;

                // check how much can be extracted
                int maxExtracted;
                try (Transaction simulatedExtract = Transaction.open(subTransaction)) {
                    maxExtracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtract);
                }

                if (maxExtracted == 0) continue;

                try (Transaction transferTransaction = Transaction.open(subTransaction)) {
                    // check how much can be inserted
                    int inserted = to.insert(fromResource, maxExtracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
                        continue;

                    totalMoved += inserted;
                    transferTransaction.commit();

                    //if we have the amount we are targeting exit the for-loop
                    if (amount == totalMoved) break;
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

    // TODO: stack factory usage
//    /**
//     * Similar to {@link #move}, but instead of all resources, it will be the first one that matches the filter. While this won't be a full list, this will simplify things like {@link FluidUtil#moveFluidWithSound}
//     *
//     * @param from         The source handler. May be null.
//     * @param to           The target handler. May be null.
//     * @param filter       The filter for transferred resources.
//     *                     Only resources for which this filter returns {@code true} will be transferred.
//     *                     This filter will never be tested with an empty resource, and filters are encouraged to throw an
//     *                     exception if this guarantee is violated.
//     * @param amount       The maximum amount that will be transferred.
//     * @param transaction  The transaction context for the operation.
//     *                     Passing in {@code null} will open a root transaction, whereas passing in a transaction will
//     *                     allow you to make the final decision to commit based on the results of this method.
//     * @param stackFactory A factory the given a resource of type {@code <R>} and an amount, a stack of type {@code <S>} can be created. The return is expected to be non-null and properly be the instanced empty value for a given resource.
//     * @param <R>          the type of resource to move.
//     * @param <S>          The type of stack returned by the handler.
//     * @return a stack of type {@code <S>} typically in the form of an ResourceStack or as an example an ItemStack based on the factory provided
//     */
//    public static <R extends IResource, S> S moveFirstOrDefault(
//            @Nullable IResourceHandler<R> from,
//            @Nullable IResourceHandler<R> to,
//            Predicate<R> filter,
//            int amount,
//            R defaultResource,
//            @Nullable TransactionContext transaction,
//            IStackFactory<R, S> stackFactory) {
//        Objects.requireNonNull(filter, "Filter may not be null");
//        TransferPreconditions.checkNonNegative(amount);
//        if (amount == 0) return stackFactory.create(defaultResource, 0);
//
//        if (from == null || to == null)
//            return stackFactory.create(defaultResource, 0);
//
//        try {
//            int totalMoved = 0;
//            R lastMovedResource = defaultResource;
//
//            int size = from.size();
//
//            for (int index = 0; index < size; ++index) {
//                R fromResource = from.getResource(index);
//                if (doesNotMatch(filter, fromResource)) continue;
//
//                // check how much can be extracted
//                int extracted;
//                try (Transaction simulatedExtractTransaction = Transaction.open(transaction)) {
//                    extracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtractTransaction);
//                }
//
//                if (extracted == 0) continue;
//
//                try (Transaction transferTransaction = Transaction.open(transaction)) {
//                    // check how much can be inserted
//                    int inserted = to.insert(fromResource, extracted, transferTransaction);
//
//                    // extract it, or rollback if the amounts don't match
//                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
//                        continue;
//
//                    totalMoved += inserted;
//                    transferTransaction.commit();
//                    lastMovedResource = fromResource;
//
//                    //if we have the amount we are targeting exit the for-loop
//                    if (amount == totalMoved) break;
//                }
//
//            }
//            //lastMovedResource should be defaultResource if totalMoved was 0 as well.
//            return stackFactory.create(lastMovedResource, totalMoved);
//        } catch (Exception e) {
//            CrashReport report = CrashReport.forThrowable(e, "Moving resources between storages");
//            //noinspection DataFlowIssue
//            report.addCategory("Move details")
//                    .setDetail("Input", from::toString)
//                    .setDetail("Output", to::toString)
//                    .setDetail("Filter", filter::toString)
//                    .setDetail("Amount", amount)
//                    .setDetail("Transaction", transaction);
//            throw new ReportedException(report);
//        }
//    }
//
//    /**
//     * A helper to construct a stack of type {@code <S>} based on the resource and amount at the specified index.
//     *
//     * @param <R> the type of resource to move.
//     * @param <S> The type of stack returned by the handler.
//     * @return a stack of type {@code <S>} typically in the form of an ResourceStack or as an example an ItemStack based on the factory provided
//     */
//    public static <R extends IResource, S> S getStackAt(IResourceHandler<R> handler, int index, IStackFactory<R, S> stackFactory) {
//        R resource = handler.getResource(index);
//        int amount = handler.getAmount(index);
//        //Handles the negative checks and throws
//        ResourceHandlerUtil.isEmpty(resource, amount);
//        return stackFactory.create(resource, amount);
//    }

    // TODO: int <-> long switching
//    public static <T extends IResource> int getAmount(IResourceHandler<T> handler) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            sum += handler.getAmount(index);
//            if (sum >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
//        }
//        return Ints.saturatedCast(sum);
//    }
//
//    public static <T extends IResource> int getAmount(IResourceHandler<T> handler, T resource) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            if (!resource.equals(handler.getResource(index))) continue;
//            sum += handler.getAmount(index);
//            if (sum >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
//        }
//        return Ints.saturatedCast(sum);
//    }
//
//    public static <T extends IResource> int getCapacity(IResourceHandler<T> handler) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            sum += handler.getCapacity(index, handler.getResource(index));
//            if (sum >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
//        }
//        return Ints.saturatedCast(sum);
//    }
//
//    public static <T extends IResource> int getCapacity(IResourceHandler<T> handler, T resource) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            sum += handler.getCapacityAsLong(index, resource);
//            if (sum >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
//        }
//        return Ints.saturatedCast(sum);
//    }
//
//    public static <T extends IResource> long getAmountAsLong(IResourceHandler<T> handler) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            sum += handler.getAmountAsLong(index);
//            if (sum < 0) return Long.MAX_VALUE;
//        }
//        return sum;
//    }
//
//    public static <T extends IResource> long getAmountAsLong(IResourceHandler<T> handler, T resource) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            if (!resource.equals(handler.getResource(index))) continue;
//            sum += handler.getAmountAsLong(index);
//            if (sum < 0) return Long.MAX_VALUE;
//        }
//        return sum;
//    }
//
//    public static <T extends IResource> long getCapacityAsLong(IResourceHandler<T> handler) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            sum += handler.getCapacityAsLong(index, handler.getResource(index));
//            if (sum < 0) return Long.MAX_VALUE;
//        }
//        return sum;
//    }
//
//    public static <T extends IResource> long getCapacityAsLong(IResourceHandler<T> handler, T resource) {
//        long sum = 0L;
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            sum += handler.getCapacityAsLong(index, resource);
//            if (sum < 0) return Long.MAX_VALUE;
//        }
//        return sum;
//    }

    /**
     * {@return {@code true} if the given resource is in the resource handler (though not necessarily interactable), {@code false} otherwise}
     */
    public static <T extends IResource> boolean contains(IResourceHandler<T> handler, T resource) {
        return indexOf(handler, resource) != -1;
    }

    /**
     * {@return the first index that contains the given resource, or -1 if no index contains it}
     */
    public static <T extends IResource> int indexOf(IResourceHandler<T> handler, T resource) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index)))
                return index;
        }
        return -1;
    }

    // TODO: unsafe access to tx state
//    public static <T extends IResource> boolean hasExtractableResource(IResourceHandler<T> handler, Predicate<T> filter) {
//        try (Transaction temp = UnsafeTransactionManager.openUnsafe()) {
//            //Simulated: we don't commit
//            int size = handler.size();
//            for (int index = 0; index < size; index++) {
//                T resource = handler.getResource(index);
//                if (!doesNotMatch(filter, resource) && handler.extract(resource, 1, temp) > 0)
//                    return true;
//            }
//            return false;
//        }
//    }

    // TODO: unsafe access to tx state
//    public static <T extends IResource> boolean hasExtractableResourceAtIndex(IResourceHandler<T> handler, Predicate<T> filter, int index) {
//        try (Transaction temp = UnsafeTransactionManager.openUnsafe()) {
//            //Simulated: we don't commit
//            T resource = handler.getResource(index);
//            return !doesNotMatch(filter, resource) && handler.extract(resource, 1, temp) > 0;
//        }
//    }
//
//    public static <T extends IResource> boolean hasExtractableResource(IResourceHandler<T> handler, T resource) {
//        try (Transaction temp = UnsafeTransactionManager.openUnsafe()) {
//            //Simulated: we don't commit
//            return handler.extract(resource, 1, temp) > 0;
//        }
//    }

    // TODO: do we want it?
//    public static <T extends IResource> T getFirstResourceOrDefault(IResourceHandler<T> handler, T defaultResource) {
//        int size = handler.size();
//        for (int index = 0; index < size; index++) {
//            T resource = handler.getResource(index);
//            if (!resource.isEmpty())
//                return resource;
//        }
//        return defaultResource;
//    }
}
