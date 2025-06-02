/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.function.Predicate;

public final class ResourceHandlerUtil {
    /**
     * PR NOTES: This is likely going to be a point of contention; do we limit a dev to be locked in at a readable number or do we let them have ~147million more points of data?
     * Realistically, it should be in favor of the player to present them the best options for readability; without sacrificing the functional aspects. The effective max limit is still an integer max,
     * but this is to try to encourage a more human approach for the player's sake. It should be assumed these numbers are very much visible to the player. At the point where 2 billion is not enough for a single call, something has gone awry earlier than just "insert" or "extract".
     * <p>
     * A near max int value intended to be easier to view in normal gameplay. (2E9)
     * While {@link Integer#MAX_VALUE} does tend to make sense as a structural upper bound it is far to often used at the player's expense of reading.
     * Anything breaking past this boundary (whether it is by storing a long internally or otherwise), the main request is to maintain rapid human readability.
     * <p>
     * <strong>Key point:</strong> Human readable
     */
    public static final int MAX = 2000000000;
    public static final String MAX_RESOURCE_SIZE_STRING = "2,000,000,000";

    public static <T extends IResource> boolean isInvalidInquiry(T resource, int amount) {
        return resource.isEmpty() || amount <= 0;
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
        for (int i = 0; i < handler.size(); i++) {
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
        for (int i = 0; i < handler.size(); i++) {
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
        return handler.getResource(index).isEmpty() || handler.getAmount(index) == 0;
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
        return handler.getAmount(index) >= handler.getCapacity(index, handler.getResource(index));
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
    public static <T extends IResource> int insertStacking(IResourceHandler<T> handler, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, @Nullable TransactionContext transaction) {
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
    public static <T extends IResource> int insertIndexForced(IResourceHandler<T> handler, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, @Nullable TransactionContext transaction) {
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
    public static <T extends IResource> int extract(IResourceHandler<T> handler, T resource, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, @Nullable TransactionContext transaction) {
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
        try (var transaction = Transaction.open(TransactionContext.EMPTY)) {
            //We don't commit allow us to just inquiry the amount
            return extract(handler, resource, ResourceHandlerUtil.MAX, null);
        }
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to extract the resource from
     * @param filter      the filter to apply to the resources
     * @param amount      the desired amount of the resource to extract
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> IResourceStack<T> extractFiltered(IResourceHandler<T> handler, Predicate<T> filter, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, IResourceStack<T> emptyResource, @Nullable TransactionContext transaction) {
        try (var subTransaction = Transaction.open(transaction)) {

            int size = handler.size();
            var handled = 0;
            var resourceTarget = emptyResource.resource();
            for (int index = 0; index < size; index++) {
                T resource = handler.getResource(index);
                if (resource.isEmpty()) continue;
                if (!filter.test(resource)) continue;
                if (resourceTarget.isEmpty())
                    resourceTarget = resource;
                else if (!resourceTarget.equals(resource)) continue;

                handled += handler.extract(resource, amount - handled, subTransaction);
                if (handled == amount) {
                    subTransaction.commit();
                    return new ResourceStack<>(resource, handled);
                }
            }

            subTransaction.commit();
            if (handled > 0)
                return new ResourceStack<>(resourceTarget, handled);
            return emptyResource;
        }
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to extract the resource from
     * @param index       the index to use for the handler
     * @param filter      the filter to apply to the resources
     * @param amount      the desired amount of the resource to extract
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> IResourceStack<T> extractIndexFiltered(IResourceHandler<T> handler, int index, Predicate<T> filter, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, IResourceStack<T> emptyResource, @Nullable TransactionContext transaction) {
        try (var subTransaction = Transaction.open(transaction)) {
            T resource = handler.getResource(index);
            if (!filter.test(resource)) return new ResourceStack<>(resource, amount);
            int extract = handler.extract(resource, amount, subTransaction);

            subTransaction.commit();

            if (extract > 0) return new ResourceStack<>(resource, extract);
            return emptyResource;
        }
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that is not empty.
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to extract the resource from
     * @param amount      the desired amount of the resource to extract
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> IResourceStack<T> extractIndexedAny(IResourceHandler<T> handler, int index, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, IResourceStack<T> emptyResource, @Nullable TransactionContext transaction) {
        return extractIndexFiltered(handler, index, Predicate.not(IResource::isEmpty), amount, emptyResource, transaction);
    }


    /**
     * Extracts the first resource from an {@link IResourceHandler} that is not empty.
     *
     * @param <T>         the type of resource handled by the handler
     * @param handler     the {@link IResourceHandler} to extract the resource from
     * @param amount      the desired amount of the resource to extract
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> IResourceStack<T> extractAny(IResourceHandler<T> handler, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, IResourceStack<T> emptyResource, @Nullable TransactionContext transaction) {
        return extractFiltered(handler, Predicate.not(IResource::isEmpty), amount, emptyResource, transaction);
    }


    /////////////////////////////////////
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
     * @return The total amount of resources that was successfully transferred.
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    public static <T extends IResource> int move(@Nullable IResourceHandler<T> from, @Nullable IResourceHandler<T> to, Predicate<T> filter, int amount, @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (from == null || to == null) return 0;


        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                var fromResource = from.getResource(index);
                if (fromResource.isEmpty()) {
                    continue;
                }
                if (!filter.test(fromResource)) continue;

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
     * Same as {@link #move}, but some scenarios require to know what resource was moved. While this won't be a full list, this will simplify things like {@link FluidUtil#moveFluidWithSound}
     */
    public static <T extends IResource> ResourceStack<T> moveOrDefault(@Nullable IResourceHandler<T> from, @Nullable IResourceHandler<T> to, Predicate<T> filter, int amount, @Nullable TransactionContext transaction, ResourceStack<T> emptyStack) {
        Objects.requireNonNull(filter, "Filter may not be null");
        if (from == null || to == null) return emptyStack;

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int totalMoved = 0;
            T lastMovedResource = emptyStack.resource();
            int size = from.size();

            for (int index = 0; index < size; ++index) {
                var fromResource = from.getResource(index);
                if (fromResource.isEmpty()) {
                    continue;
                }
                if (!filter.test(fromResource)) continue;
                // check how much can be extracted
                int extracted;
                try (var simulatedExtract = Transaction.open(subTransaction)) {
                    extracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtract);
                }

                try (Transaction transferTransaction = Transaction.open(subTransaction)) {
                    // check how much can be inserted
                    var inserted = to.insert(fromResource, extracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (from.extract(index, fromResource, inserted, transferTransaction) == inserted) {
                        totalMoved += inserted;
                        transferTransaction.commit();
                        lastMovedResource = fromResource;
                    }
                }

                if (amount == totalMoved) {
                    // early return if nothing can be moved anymore
                    subTransaction.commit();
                    return new ResourceStack<>(fromResource, totalMoved);
                }
            }
            if (totalMoved == 0) return emptyStack;

            subTransaction.commit();
            return new ResourceStack<>(lastMovedResource, totalMoved);

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

    /////////////////////////////////////


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
    public static <T extends IResource> int move(IResourceHandler<T> from, IResourceHandler<T> to, @Range(from = 1, to = ResourceHandlerUtil.MAX) int amount, @Nullable TransactionContext transaction) {
        return move(from, to, Predicate.not(IResource::isEmpty), amount, transaction);
    }

    public static <T extends IResource> boolean hasResource(IResourceHandler<T> handler, T resource) {
        try (var temp = Transaction.open(null)) {
            //Simulated, we don't commit on an inquiry
            return handler.extract(resource, 1, temp) > 0;
        }
    }


    private ResourceHandlerUtil() { }
}
