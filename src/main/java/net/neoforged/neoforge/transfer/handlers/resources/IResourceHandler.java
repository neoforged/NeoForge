/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import com.google.common.primitives.Ints;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
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
 * <p>Out-of-bounds access using methods that accept an {@code int slot} will usually throw an exception,
 * so only indices between 0 (included) and the size (excluded) should be used.
 * If a storage has a dynamic size, it should be lenient to accommodate for callers
 * holding onto a previously returned size.
 *
 * @param <T> The type of resource this handler manages.
 */
public interface IResourceHandler<T extends IResource> {
    /**
     * {@return the <i>current</i> number of indices in this resource handler}
     *
     * <p>An index is synonymous with "slot", "tank", "buffer", etc.
     *
     * <p>Note that the size of a resource handler can change.
     */
    int size();

    /**
     * {@return the resource at the given index}
     *
     * @param index The index to get the resource from.
     */
    T getResource(int index);

    /**
     * Returns the amount of the {@link #getResource currently stored resource} at the given index.
     *
     * <p>In general, resource handlers can report {@code long} amounts.
     * However, if the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@link #getAmountAsInt int-returning overload} can be used instead.
     *
     * <p>The returned amount should be <strong>non-negative</strong>, and should never surpass the {@link #getCapacity capacity} of the same index.
     *
     * @param index The index to get the amount from.
     * @return the amount at the given index
     * @see #getAmountAsInt(int)
     */
    long getAmount(int index);

    /**
     * Returns the amount of the {@link #getResource currently stored resource} at the given index, as an {@code int}.
     *
     * <p>This is a convenience method to clamp the amount to an {@code int},
     * for the cases where the handler is known to only support amounts up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     *
     * <p>The returned amount should be <strong>non-negative</strong>, and should never surpass the {@link #getCapacityAsInt capacity} of the same index.
     *
     * @param index The index to get the amount from.
     * @return the amount at the given index, as an int
     * @see #getAmount(int) the long-returning overload
     */
    @ApiStatus.NonExtendable
    default int getAmountAsInt(int index) {
        return Ints.saturatedCast(getAmount(index));
    }

    /**
     * Returns the capacity of the handler at the given index and for the given resource,
     * irrespective of the current amount or resource currently at that index.
     * <p>
     * In general, resource handlers can report {@code long} capacities.
     * However, if the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only,
     * the {@link #getCapacityAsInt int-returning overload} can be used instead.
     * <p>
     * While passing in resources that would return {@code false} on {@link #isValid(int, IResource)}, it should be expected to always return 0.
     * <p>
     * This function serves as metadata only, and its result might be approximate. The only way to know if a handler will accept a resource, is to try to {@link #insert insert} it.
     *
     * @param index    The index to get the limit for.
     * @param resource The resource to get the limit for. May be empty to return a generic limit.
     * @return the capacity at the given index
     * @see #getCapacityAsInt(int, IResource)
     */
    // TODO: remark that the amount should be larger than the capacity?
    long getCapacity(int index, T resource);

    /**
     * Returns the capacity of the handler at the given index and for the given resource,
     * irrespective of the current amount or resource currently at that index, as an {@code int}.
     * <p>
     * This is a convenience method to clamp the capacity to an {@code int},
     * for the cases where the handler is known to only support capacities up to {@code Integer.MAX_VALUE},
     * or if the caller prefers to deal in {@code int}s only.
     * <p>
     * While passing in resources that would return {@code false} on {@link #isValid(int, IResource)}, it should be expected to always return 0.
     * <p>
     * This function serves as metadata only, and its result might be approximate.
     * The only way to know if a handler will accept a resource, is to try to {@link #insert insert} it.
     *
     * @param index    The index to get the limit for.
     * @param resource The resource to get the limit for. May be empty to return a generic limit.
     * @return the capacity at the given index, as an int
     * @see #getCapacity(int, IResource)
     */
    @ApiStatus.NonExtendable
    default int getCapacityAsInt(int index, T resource) {
        return Ints.saturatedCast(getCapacity(index, resource));
    }

    /**
     * {@return whether the given resource is generally allowed to be contained in the handler at the given index,
     * irrespective of the current amount or resource currently at that index}
     * <p>
     * This function serves as metadata only, and its result might be approximate.
     * The only way to know if a handler will accept a resource, is to try to {@link #insert insert} it.
     *
     * @param index    The index to check.
     * @param resource The resource to check. Must not be empty, or an exception will typically be thrown.
     */
    boolean isValid(int index, T resource);

    /**
     * Tries to insert up to some amount of a resource into the handler at the given index.
     *
     * <p>Changes to the handler are made in the context of a {@link Transaction},
     * and are expected to properly support rollbacks/reversions, see also {@link SnapshotJournal}.
     *
     * @param index       The index to insert the resource into.
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was inserted.
     * @see #insert(IResource, int, TransactionContext) Inserting into any index in the handler
     */
    int insert(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Tries to insert up to some amount of a resource into the handler.
     *
     * <p>This function is preferred to the {@link #insert(int, IResource, int, TransactionContext) index-specific overload}
     * since it lets the handler decide how to distribute the resource.
     * It can also be a lot more efficient than calling the index-specific overload in a loop,
     * as resource handlers with a special internal structure can optimize it.
     *
     * <p>Changes to the handler are made in the context of a {@link Transaction},
     * and are expected to properly support rollbacks/reversions, see also {@link SnapshotJournal}.
     *
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was inserted.
     * @see #insert(int, IResource, int, TransactionContext) Inserting into a specific index of the handler
     */
    default int insert(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

        int inserted = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            inserted += insert(index, resource, amount - inserted, transaction);
            if (inserted == amount) break;
        }
        return inserted;
    }

    /**
     * Tries to extract up to some amount of a resource from the handler at the given index.
     *
     * <p>Changes to the handler are made in the context of a {@link Transaction},
     * and are expected to properly support rollbacks/reversions, see also {@link SnapshotJournal}.
     *
     * @param index       The index to extract the resource from.
     * @param resource    The resource to extract. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was extracted.
     * @see #extract(IResource, int, TransactionContext) Extracting from any index in the handler
     */
    int extract(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Tries to extract up to some amount of a resource from the handler.
     *
     * <p>This function is preferred to the {@link #extract(int, IResource, int, TransactionContext) index-specific overload}
     * since it lets the handler decide how to select the resource.
     * It can also be a lot more efficient than calling the index-specific overload in a loop,
     * as resource handlers with a special internal structure can optimize it.
     *
     * <p>Changes to the handler are made in the context of a {@link Transaction},
     * and are expected to properly support rollbacks/reversions, see also {@link SnapshotJournal}.
     *
     * @param resource    The resource to extract. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return A non-negative integer not greater than {@code amount}: the amount that was extracted.
     * @see #extract(int, IResource, int, TransactionContext) Extracting from a specific index of the handler
     */
    default int extract(T resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonBlank(resource, amount);

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
    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
