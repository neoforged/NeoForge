/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A generic handler for the transfer and storage of {@link IResource resources} whether it be inserting, extracting, querying some value, etc.
 *
 * <h2>Indices</h2>
 * <p>A resource handler is organized into indices, which are addressed using an int between {@code 0} and {@code size() - 1}.
 * <p>An index represents a "slot", "tank", "buffer", depending on the type of resource.
 * <p>Out-of-bounds access using methods that accept an {@code index} will usually throw an exception,
 * so only indices between 0 (included) and the size (excluded) should be used.
 * If a storage has a dynamic size, it should be lenient to accommodate for callers
 * holding onto a previously returned size.
 *
 * @param <T> The type of resource this handler manages.
 */
public interface ResourceHandler<T extends IResource> {
    /**
     * {@return the <i>current</i> number of indices in this resource handler}
     *
     * <p>This size provides a bound on the valid indices for this handler,
     * see the documentation of {@link ResourceHandler}.
     *
     * <p>Note that the size of a resource handler can change.
     * In that case, the handler is expected to be lenient with its index checks,
     * in case the caller is holding onto a previously returned size.
     */
    int size();

    /**
     * {@return the resource at the given index, which may be empty}
     *
     * @param index The index to get the resource from.
     */
    T getResource(int index);

    /**
     * Returns the amount of the {@linkplain #getResource currently stored resource} at the given index, as a {@code long}.
     *
     * <p>In general, resource handlers can report {@code long} amounts.
     * However, if the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@linkplain #getAmountAsInt int-returning overload} can be used instead.
     *
     * <p>The returned amount must be <strong>non-negative</strong>, and should never surpass the {@linkplain #getCapacityAsLong capacity} of the same index.
     *
     * @param index The index to get the amount from.
     * @return the amount at the given index, as a long
     * @see #getAmountAsInt(int)
     */
    long getAmountAsLong(int index);

    /**
     * Returns the amount of the {@linkplain #getResource currently stored resource} at the given index, as an {@code int}.
     *
     * <p>This is a convenience method to clamp the amount to an {@code int},
     * for the cases where the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     *
     * <p>The returned amount must be <strong>non-negative</strong>, and should never surpass the {@linkplain #getCapacityAsInt capacity} of the same index.
     *
     * @implNote This method should not be implemented. The default method will call {@link #getAmountAsLong(int)} and convert the result appropriately.
     * @param index The index to get the amount from.
     * @return the amount at the given index, as an {@code int}
     * @see #getAmountAsLong(int) the long-returning overload
     */
    @ApiStatus.NonExtendable
    default int getAmountAsInt(int index) {
        return Ints.saturatedCast(getAmountAsLong(index));
    }

    /**
     * Returns the capacity of the handler at the given index and for the given resource,
     * irrespective of the current amount or resource currently at that index, as a {@code long}.
     * <p>
     * In general, resource handlers can report {@code long} capacities.
     * However, if the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@linkplain #getCapacityAsInt int-returning overload} can be used instead.
     * <p>
     * This function serves as metadata only, and its result might be approximate.
     * The only way to know if a handler will accept a resource, is to try to {@link #insert insert} it.
     * <p>
     * The capacity for the {@linkplain #getResource currently stored resource} should be greater than or equal to
     * the {@linkplain #getAmountAsLong(int) amount} at the same index.
     *
     * @implSpec This method should return 0 for any resource for which {@link #isValid(int,IResource)} returns {@code false}.
     * @param index    The index to get the capacity for.
     * @param resource The resource to get the capacity for. May be empty to get the general capacity at the index.
     * @return the capacity at the given index, as a long
     * @see #getCapacityAsInt(int, IResource)
     */
    long getCapacityAsLong(int index, T resource);

    /**
     * Returns the capacity of the handler at the given index and for the given resource,
     * irrespective of the current amount or resource currently at that index, as an {@code int}.
     * <p>
     * This is a convenience method to get the capacity clamped to an {@code int},
     * for the cases where the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     * <p>
     * This function serves as metadata only, and its result might be approximate.
     * The only way to know if a handler will accept a resource, is to try to {@linkplain #insert insert} it.
     * <p>
     * The capacity for the {@linkplain #getResource currently stored resource} should be greater than or equal to
     * the {@linkplain #getAmountAsInt(int) amount} at the same index.
     *
     * @implNote This method should not be implemented. The default method will call {@link #getCapacityAsLong(int, IResource)} and convert the result appropriately.
     * @param index    The index to get the limit for.
     * @param resource The resource to get the limit for. May be empty to get the general capacity at the index.
     * @return the capacity at the given index, as an {@code int}
     * @see #getCapacityAsLong(int, IResource)
     */
    @ApiStatus.NonExtendable
    default int getCapacityAsInt(int index, T resource) {
        return Ints.saturatedCast(getCapacityAsLong(index, resource));
    }

    /**
     * {@return whether the given resource is generally allowed to be contained at the given index,
     * irrespective of the current amount or resource currently at that index}
     * <p>
     * This method only provides a hint.
     * The only way to find out whether a handler will accept a resource, is to try {@linkplain #insert inserting} it.
     *
     * @param index    The index to check.
     * @param resource The resource to check. <strong>Must be non-empty.</strong>
     */
    boolean isValid(int index, T resource);

    /**
     * Inserts up to the given amount of a resource into the handler at the given index.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     * @implNote {@link SnapshotJournal} can serve as the base class for a transaction-aware resource handler.
     * @param index       The index to insert the resource into.
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was inserted.
     * @see #insert(IResource, int, TransactionContext) Inserting without a specific index, which can be more efficient.
     */
    int insert(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Inserts up to the given amount of a resource into the handler.
     *
     * <p>This function is preferred to the {@linkplain #insert(int, IResource, int, TransactionContext) index-specific overload}
     * since it lets the handler decide how to distribute the resource.
     * <p>This method is expected to be more efficient than callers trying to find a suitable index for insertion themselves.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     * @implNote {@link SnapshotJournal} can serve as the base class for a transaction-aware resource handler.
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was inserted.
     * @see #insert(int, IResource, int, TransactionContext) Inserting into a specific index of the handler.
     */
    default int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            inserted += insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }

    /**
     * Extracts up to the given amount of a resource from the handler at the given index.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     * @implNote {@link SnapshotJournal} can serve as the base class for a transaction-aware resource handler.
     * @param index       The index to extract the resource from.
     * @param resource    The resource to extract. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was extracted.
     * @see #extract(IResource, int, TransactionContext) Extracting without a specific index, which can be more efficient.
     */
    int extract(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Tries to extract up to the given amount of a resource from the handler.
     *
     * <p>This function is preferred to the {@linkplain #extract(int, IResource, int, TransactionContext) index-specific overload}
     * since it lets the handler decide how to find indices that contain the resource.
     * <p>This method is expected to be more efficient than callers trying to find indices that contain the resource themselves.
     *
     * <p>Changes to the handler are made in the context of a {@linkplain Transaction transaction}.
     *
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     * @implNote {@link SnapshotJournal} can serve as the base class for a transaction-aware resource handler.
     * @param resource    The resource to extract. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was extracted.
     * @see #extract(int, IResource, int, TransactionContext) Extracting from a specific index of the handler.
     */
    default int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int extracted = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            extracted += extract(index, resource, amount - extracted, transaction);
            if (extracted == amount) break;
        }
        return extracted;
    }

    /**
     * Creates a class with the right generic type, such that it can be used to register a capability.
     */
    @SuppressWarnings("unchecked")
    static <T extends IResource> Class<ResourceHandler<T>> asClass() {
        return (Class<ResourceHandler<T>>) (Object) ResourceHandler.class;
    }
}
