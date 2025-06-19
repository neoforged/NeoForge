/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.handlers.ITransactionHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A generic handler for handling a {@link IResource resource} of type {@link T} whether it be inserting, extracting, querying some size value, etc.
 * Trying to interact with a handler with an index larger than the size is expected to often times throw an {@link IndexOutOfBoundsException} or similar error. Ensure you are within the size constraints of the handler before calling a method.
 *
 * @param <T> The type of resource this handler manages.
 */
public interface IResourceHandler<T extends IResource> extends ITransactionHandler {
    /**
     * An index is synonymous with "slot", "tank", "buffer", etc.
     *
     * @return The number of indices this handler manages. <strong>Must be non-negative</strong>
     */
    int size();

    /**
     * @param index The index to get the resource from. <strong>Must be non-negative</strong>
     * @return The resource at the given index.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     */
    T getResource(int index);

    /**
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount of the resource at the given index. <strong>Must be non-negative</strong> and should never surpass capacity
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getAmountAsLong(int)
     */
    int getAmount(int index);

    /**
     * @param index The index to get the amount from. <strong>Must be non-negative</strong>
     * @return The amount as a long of the resource at the given index. <strong>Must be non-negative</strong> and must never surpass capacity
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getAmount(int)
     */
    default long getAmountAsLong(int index) {
        return getAmount(index);
    }

    /**
     * Gets the maximum capacity that the given index can handle of the given resource.
     * If an empty resource (an {@link IResource} that returns {@code true} on {@link IResource#isEmpty()}) is provided,
     * then the theoretical maximum should be returned, regardless of the return of {@link #getResource}.
     * <p>
     * While passing in resources that would return {@code false} on {@link #isValid(int, IResource)}, it should be expected to always return 0.
     * <p>
     * If the resource returned from {@link #getResource(int)} with the same index does not match, it is expected the capacity would return 0.
     *
     * @param index    The index to get the limit from. <strong>Must be non-negative</strong>
     * @param resource The resource to get the limit for. If empty, this should return the theoretical limit of that index
     * @return The limit of the resource at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacityAsLong(int, IResource)
     */
    int getCapacity(int index, T resource);

    /**
     * Gets the maximum capacity that the given index can handle as a long of the given resource.
     * If an empty resource (an {@link IResource} that returns {@code true} on {@link IResource#isEmpty()}) is provided,
     * then the theoretical maximum should be returned, regardless of the return of {@link #getResource}.
     * <p>
     * While passing in resources that would return {@code false} on {@link #isValid(int, IResource)}, it should be expected to always return 0.
     * <p>
     * If the resource returned from {@link #getResource(int)} with the same index does not match, it is expected the capacity would return 0.
     *
     * @param index    The index to get the limit from. <strong>Must be non-negative</strong>
     * @param resource The resource to get the limit for. If empty, this should return the theoretical limit of that index
     * @return The limit of the resource at the given index. <strong>Must be non-negative</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #getCapacity(int, IResource)
     */
    default long getCapacityAsLong(int index, T resource) {
        return getCapacity(index, resource);
    }

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index.
     * This is typically called in the {@link #insert(int, IResource, int, TransactionContext Context)}
     * implementations or general resource querying. However, this is separate from if the resource could
     * currently fit in the handler. This is expected to be true, even if the handler would be full.
     *
     * @param index    The index to check. <strong>Must be non-negative</strong>
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     * @throws IndexOutOfBoundsException when passing an invalid index.
     *                                   Negative indices are always invalid.
     */
    boolean isValid(int index, T resource);

    /**
     * Checks if the given index allows insertion of a resource,
     * regardless of the state of the handler. Meaning this value should not be dynamic.
     * <p>
     * Intended use is for something like a pipe graph lookup to be able to reduce
     * the runtime workload on handlers that can never do a specific operation.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to
     * be inserted into the given index, this should return true. To be clear, this value is assumed
     * to be constant throughout the life-time of the handler and
     * does <b>not</b> control the handler's logic in any way.
     * <h5>IMPORTANT:</h5>
     * Returning false, will not inherently prevent something from calling insert
     * or change the result of that call, so you will still need to handle those scenarios.
     * This is to allow things like logistics (pipes, searches, etc.) to be able to infer
     * what it can do with the handler well before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert nor calling just before you call insert.
     * This if for an early lookup spanning multiple ticks.
     * <p>
     * If your handler can change size dynamically, then it may be wise to
     * return true for this unless you know for certain a particular index would never be insertable to.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.
     * </b> (hence the note about dynamic size erring on the side of caution)
     *
     * @param index The index to check. <strong>Must be non-negative</strong>
     * @return True if the handler supports insertion to the specified index regardless of contents, false otherwise.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #supportsInsertion()
     */
    boolean supportsInsertion(int index);

    /**
     * Checks if the handler allows insertion into at least one index, regardless of the state of the handler.
     * Meaning this value should not be dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way.
     * Returning false, will not inherently prevent something from calling insert
     * or change the result of that call, so you will still need to handle those scenarios.
     * This is to allow things like logistics (pipes, searches, etc.) to be able to infer
     * what it can do with the handler before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert nor calling just before you call insert.
     * This if for an early lookup spanning multiple ticks.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b>
     * (hence the note about dynamic size erring on the side of caution)
     *
     * @return True if the handler supports insertion regardless of contents, false otherwise.
     * @see #supportsInsertion(int)
     */
    default boolean supportsInsertion() {
        var size = size();
        for (int i = 0; i < size; i++) {
            if (supportsInsertion(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given index allows extraction of a resource, regardless of the state of the handler.
     * Meaning this value should not be dynamic.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be extracted
     * from the given index, this should return true.
     * <p>
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way.
     * Returning false, will not inherently prevent something from calling extract or
     * change the result of that call, so you will still need to handle those scenarios.
     * This is to allow things like logistics (pipes, searches, etc.) to be able to infer
     * what it can do with the handler before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract nor calling just before you call extract.
     * This if for an early lookup spanning multiple ticks.
     * <p>
     * If your handler can change size dynamically, then it may be wise to return true for
     * this unless you know for certain a particular index would never be extractable from.
     *
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b>
     * (hence the note about dynamic size erring on the side of caution)
     *
     * @param index The index to check. <strong>Must be non-negative</strong>
     * @return True if the handler supports extraction from the specified index regardless of contents, false otherwise.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #supportsExtraction()
     */
    boolean supportsExtraction(int index);

    /**
     * Checks if the handler allows extraction from at least one index, regardless of the state of
     * the handler. Meaning this value should not be dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way.
     * Returning false, will not inherently prevent something from calling extract
     * or change the result of that call, so you will still need to handle those scenarios.
     * This is to allow things like logistics (pipes, searches, etc.) to be able to infer
     * what it can do with the handler before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract nor calling just before you call extract.
     * This if for an early lookup spanning multiple ticks.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b>
     * (hence the note about dynamic size erring on the side of caution)
     *
     * @return True if the handler supports extraction regardless of contents, false otherwise.
     * @see #supportsExtraction(int)
     */
    default boolean supportsExtraction() {
        var size = size();
        for (int i = 0; i < size; i++) {
            if (supportsExtraction(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index       The index to insert the resource into. <strong>Must be non-negative</strong>
     * @param resource    The resource to insert.
     * @param amount      The amount of the resource to insert. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext} transaction to be inserting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount of the resource that was inserted. <strong>Must be non-negative.</strong>
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #insert(IResource, int, TransactionContext) Inserting into any index in the handler
     */
    int insert(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #insert(int, IResource, int, TransactionContext)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#insert(IResource, int, TransactionContext) ResourceStorage.insertBehaviour} for an example.
     *
     * @param resource    The resource to insert. <strong>Must be non-negative</strong>
     * @param amount      The amount of the resource to insert. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext } transaction to be inserting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount (Must be non-negative) of the resource that was inserted.
     * @see #insert(int, IResource, int, TransactionContext) Inserting by index
     */
    int insert(T resource, int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index       The index to extract the resource from. <strong>Must be non-negative</strong>
     * @param resource    The resource to extract.
     * @param amount      The amount of the resource to extract. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext} transaction to be extracting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount (Must be non-negative) of the resource that was extracted.
     * @throws IndexOutOfBoundsException when passing an invalid index. Negative indices are always invalid.
     * @see #extract(IResource, int, TransactionContext) Extracting from any index in the handler
     */
    int extract(int index, T resource, int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #extract(int, IResource, int, TransactionContext)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#extract(IResource, int, TransactionContext) ResourceStorage.extractBehaviour} for an example.
     *
     * @param resource    The resource to extract.
     * @param amount      The amount of the resource to extract. <strong>Must be non-negative</strong>
     * @param transaction The {@link TransactionContext } transaction to be extracting with.
     *                    It is expected that the handler properly supports rollbacks/reversions with a {@link SnapshotJournal}
     * @return The amount (Must be non-negative) of the resource that was extracted.
     * @see #extract(int, IResource, int, TransactionContext) Extracting by index
     */
    int extract(T resource, int amount, TransactionContext transaction);

    /**
     * <p>
     * Example:
     *
     * <pre>{@code
     * public static final BlockCapability<IResourceHandler<FluidResource>, @Nullable Direction> BLOCK = BlockCapability.createSided(create("fluid_handler"), IResourceHandler.asClass());
     *
     * }</pre>
     *
     * @return a class type ready to be used by something like the capability token registry.
     */
    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        //noinspection unchecked
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
