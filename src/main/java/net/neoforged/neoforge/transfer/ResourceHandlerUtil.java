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
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for dealing with {@link ResourceHandler}.
 */
public final class ResourceHandlerUtil {
    private ResourceHandlerUtil() {}

    /**
     * Determines if either the given resource or amount is classified as empty: if either {@link Resource#isEmpty()} is {@code true},
     * or the amount is zero (or negative) then the resource is considered empty.
     *
     * @param resource The resource to check.
     * @param amount   An amount to check.
     * @return {@code true} if either {@link Resource#isEmpty()} returns {@code true}, or the amount is {@code <= 0}.
     */
    public static boolean isEmpty(Resource resource, int amount) {
        return amount <= 0 || resource.isEmpty();
    }

    /**
     * Checks if a {@link ResourceHandler} is empty.
     *
     * <p>A {@link ResourceHandler} is considered empty if it has an amount of zero at all its indices.
     *
     * <p>A handler of size zero will always be considered empty.
     *
     * @param handler the {@link ResourceHandler} to check for emptiness
     * @return {@code true} if the {@link ResourceHandler} is empty, {@code false} otherwise
     */
    public static boolean isEmpty(ResourceHandler<? extends Resource> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmountAsLong(i) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a {@link ResourceHandler} is full.
     * <p>
     * A {@code IResourceHandler} is considered full if all of its indices contain resources with amounts
     * greater than or equal to their respective {@linkplain ResourceHandler#getCapacityAsLong(int, Resource) capacity}.
     * <p>A handler of size zero is always considered full.
     *
     * @param handler the {@link ResourceHandler} to check
     * @return {@code true} if the {@link ResourceHandler} is full, {@code false} otherwise
     */
    public static <T extends Resource> boolean isFull(ResourceHandler<T> handler) {
        int size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.getAmountAsLong(i) < handler.getCapacityAsLong(i, handler.getResource(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the given resource {@link ResourceHandler#isValid is valid} for any index of the given resource handler.
     *
     * @param handler  the {@link ResourceHandler} to check
     * @param resource the resource to check. <strong>Must be non-empty.</strong>
     * @return {@code true} if the resource is valid in any index of the handler.
     * @see ResourceHandler#isValid(int, Resource)
     */
    public static <T extends Resource> boolean isValid(ResourceHandler<T> handler, T resource) {
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
     * Calculates the redstone signal strength based on the given resource handler's content. This value is between 0 and 15.
     * <p>This method is based on {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}.
     *
     * @param handler the resource handler to calculate the signal from
     * @param <T>     the type of resource handled by the handler
     * @return the redstone signal strength
     */
    public static <T extends Resource> int getRedstoneSignalFromResourceHandler(ResourceHandler<T> handler) {
        float proportion = 0.0F;
        int sampleCount = 0; // Number of samples in proportion
        int size = handler.size();
        for (int index = 0; index < size; ++index) {
            long indexFill = handler.getAmountAsLong(index);
            if (indexFill > 0) {
                long capacity = handler.getCapacityAsLong(index, handler.getResource(index));
                if (capacity > 0) {
                    // Clamp to 1 to avoid overfilled slots increasing the signal strength beyond 15
                    proportion += Math.min(1.0f, (float) indexFill / capacity);
                    sampleCount++;
                }
            }
        }

        if (sampleCount == 0) {
            return Redstone.SIGNAL_NONE;
        }

        proportion /= sampleCount;
        return Mth.lerpDiscrete(proportion, Redstone.SIGNAL_NONE, Redstone.SIGNAL_MAX);
    }

    /**
     * Tries to insert up to some amount of a resource into the handler,
     * using stacking logic: resources will be inserted into filled indices first, then empty indices.
     *
     * @param handler     the {@link ResourceHandler} to insert the resource into. can be {@code null}, which makes this method a no-op.
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     *                    Passing in {@code null} will open a root transaction, and commit it at the end of the method.
     * @return the amount of the resource that was inserted
     */
    public static <T extends Resource> int insertStacking(
            @Nullable ResourceHandler<T> handler,
            T resource,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (handler == null || amount == 0) return 0;

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
            if (inserted < amount) {
                for (int index = 0; index < size; index++) {
                    if (handler.getResource(index).isEmpty()) {
                        inserted += handler.insert(index, resource, amount - inserted, tx);
                        if (inserted == amount) break;
                    }
                }
            }

            tx.commit();
            return inserted;
        }
    }

    /**
     * Extracts the first resource from a {@link ResourceHandler} that is not empty and matches the given filter.
     *
     * @param <T>         The type of resource handled by the handler
     * @param handler     The {@link ResourceHandler} to extract the resource from. Can be {@code null}, which makes this method a no-op.
     * @param filter      The first non-empty resource for which this filter returns {@code true} will be extracted.
     * @param amount      The desired amount of the resource to extract
     * @param transaction The transaction context for the operation.
     *                    Passing in {@code null} will open a root transaction, whereas passing in a transaction will
     *                    allow you to make the final decision to commit based on the results of this method.
     * @return the resource and amount that was extracted or {@code null} if nothing was extracted
     */
    @Nullable
    public static <T extends Resource> ResourceStack<T> extractFirst(
            @Nullable ResourceHandler<T> handler,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);
        if (handler == null || amount == 0) return null;

        // We are potentially iterating through the indices twice:
        // a first time in findExtractableResource and a second time in the extract call below.
        // This is done for simplicity of implementation, and to give the handler a chance to optimize the extract call.
        // This can be reconsidered if finding the resource and extracting all copies of it in a single iteration pass
        // would turn out to be generally faster.
        T resource = findExtractableResource(handler, filter, transaction);
        if (resource == null) return null;

        try (var tx = Transaction.open(transaction)) {
            int extracted = handler.extract(resource, amount, tx);
            if (extracted > 0) {
                tx.commit();
                return new ResourceStack<>(resource, extracted);
            } else {
                return null;
            }
        }
    }

    /**
     * Move resources matching a given filter between two resource handlers, and return the amount that was successfully moved.
     *
     * <h3>Usage Example</h3>
     *
     * <pre>{@code
     * // Source
     * IResourceHandler<FluidResource> source;
     * // Target
     * IResourceHandler<FluidResource> target;
     *
     * // Move exactly one bucket of water:
     * try (Transaction transaction = Transaction.open(null)) {
     *     int waterMoved = ResourceHandlerUtil.move(source, target, fr -> fr.is(Fluids.WATER), FluidType.BUCKET_VOLUME, transaction);
     *     if (waterMoved == FluidType.BUCKET_VOLUME) {
     *         // Only commit if exactly one bucket was moved.
     *         transaction.commit();
     *     }
     *     // Leaving this try-block will keep changes only when the transaction was committed.
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
     * @return the resource and amount that was transferred or {@code null} if nothing was transferred
     * @throws IllegalStateException If no transaction is passed and a transaction is already active on the current thread.
     */
    public static <T extends Resource> int move(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0) return 0;

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

                    // extract it, or rollback if we cannot actually extract the amount we inserted
                    // this can happen even for a well-behaving handler if it only supports extracting the exact
                    // amount we previously simulated, but the destination only accepted less.
                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
                        continue;

                    totalMoved += inserted;
                    transferTransaction.commit();

                    //if we have the amount we are targeting exit the for-loop
                    if (totalMoved >= amount) break;
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
     * Similar to {@link #move}, but transfers only the first resource that matches the filter and can be
     * successfully transferred.
     *
     * @param from        The source handler. May be null.
     * @param to          The target handler. May be null.
     * @param filter      The filter for transferred resources.
     *                    Only resources for which this filter returns {@code true} will be transferred.
     *                    This filter will never be tested with an empty resource.
     * @param amount      The maximum amount that will be transferred.
     * @param transaction The transaction context for the operation.
     *                    Passing in {@code null} will open a root transaction, whereas passing in a transaction will
     *                    allow you to make the final decision to commit based on the results of this method.
     * @param <T>         the type of resource to move.
     * @return the resource and amount that was transferred or {@code null} if nothing was transferred
     */
    @Nullable
    public static <T extends Resource> ResourceStack<T> moveFirst(
            @Nullable ResourceHandler<T> from,
            @Nullable ResourceHandler<T> to,
            Predicate<T> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        Objects.requireNonNull(filter, "Filter may not be null");
        TransferPreconditions.checkNonNegative(amount);
        if (from == null || to == null || amount == 0)
            return null;

        try {
            int totalMoved = 0;
            T selectedResource = null;

            int size = from.size();

            for (int index = 0; index < size; ++index) {
                T fromResource = from.getResource(index);

                // Either select the first resource that matches the filter, or continue moving if it matches the previously selected resource.
                if (selectedResource == null && (fromResource.isEmpty() || !filter.test(fromResource))
                        || selectedResource != null && !selectedResource.equals(fromResource)) {
                    continue;
                }

                // check how much can be extracted
                int extracted;
                try (Transaction simulatedExtractTransaction = Transaction.open(transaction)) {
                    extracted = from.extract(index, fromResource, amount - totalMoved, simulatedExtractTransaction);
                }

                if (extracted == 0) continue;

                try (Transaction transferTransaction = Transaction.open(transaction)) {
                    // check how much can be inserted
                    int inserted = to.insert(fromResource, extracted, transferTransaction);

                    // The target might not accept the resource at all, or might be full
                    if (inserted == 0) continue;

                    // extract it, or rollback if we cannot actually extract the amount we inserted
                    // this can happen even for a well-behaving handler if it only supports extracting the exact
                    // amount we previously simulated, but the destination only accepted less.
                    if (inserted != from.extract(index, fromResource, inserted, transferTransaction))
                        continue;

                    totalMoved += inserted;
                    transferTransaction.commit();
                    selectedResource = fromResource;

                    //if we have the amount we are targeting exit the for-loop
                    if (totalMoved >= amount) break;
                }

            }

            return totalMoved > 0 ? new ResourceStack<>(selectedResource, totalMoved) : null;
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

    /**
     * {@return {@code true} if the resource handler contains the given resource, {@code false} otherwise}
     *
     * @param handler  The handler to check for the resource.
     * @param resource The resource to check for. <strong>Must be non-empty.</strong>
     */
    public static <T extends Resource> boolean contains(ResourceHandler<T> handler, T resource) {
        return indexOf(handler, resource) != -1;
    }

    /**
     * {@return the first index that contains the given resource, or -1 if no index contains it}
     *
     * @param handler  The handler to check for the resource.
     * @param resource The resource to find. <strong>Must be non-empty.</strong>
     */
    public static <T extends Resource> int indexOf(ResourceHandler<T> handler, T resource) {
        TransferPreconditions.checkNonEmpty(resource);
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (resource.equals(handler.getResource(index)))
                return index;
        }
        return -1;
    }

    /**
     * Checks if the given resource handler has at least one resource that matches the given filter and can be extracted.
     *
     * @param handler     The handler to check for resources.
     * @param filter      A filter that will be applied to each non-empty resource in the handler. Only resources for which this filter returns {@code true} will be considered.
     * @param transaction The transaction that this operation is part of.
     *                    This method will always use a nested transaction that will be rolled back.
     *                    {@code null} can be passed to conveniently have this method open its own root transaction.
     * @return The first non-empty resource that matches the filter and is extractable, or {@code null} otherwise.
     * @param <T> The type of resource handled by the handler.
     */
    @Nullable
    public static <T extends Resource> T findExtractableResource(
            ResourceHandler<T> handler,
            Predicate<T> filter,
            @Nullable TransactionContext transaction) {
        try (Transaction temp = Transaction.open(transaction)) {
            int size = handler.size();
            for (int index = 0; index < size; index++) {
                T resource = handler.getResource(index);
                if (!resource.isEmpty() && filter.test(resource) && handler.extract(index, resource, handler.getAmountAsInt(index), temp) > 0) {
                    return resource;
                }
            }
            return null;
        }
    }
}
