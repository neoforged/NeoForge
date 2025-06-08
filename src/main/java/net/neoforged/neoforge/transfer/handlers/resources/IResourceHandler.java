/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.resources;

import javax.annotation.Nonnegative;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A generic handler for handling a {@link IResource resource} of type {@link T}.
 *
 * @param <T> The type of resource this handler manages.
 */
public interface IResourceHandler<T extends IResource> extends ITransactionHandler {
    int MAX_VALUE = Integer.MAX_VALUE;

    /**
     * An index in synonymous with "slot", "tank", "buffer", etc.
     *
     * @return The number of indices this handler manages.
     */
    int size();

    /**
     * @param index The index to get the resource from.
     * @return The resource at the given index.
     */
    T getResource(@Nonnegative int index);

    /**
     * @param index The index to get the amount from.
     * @return The amount of the resource at the given index. Must be non-negative
     */
    int getAmount(@Nonnegative int index);

    @Nonnegative
    default long getAmountAsLong(@Nonnegative int index) {
        return getAmount(index);
    }

    /**
     * Gets the maximum capacity that the given index can handle of the given resource.
     * If an empty resource (an {@link IResource} that returns {@code true} on {@link IResource#isEmpty()}) is provided,
     * then the theoretical maximum should be returned, regardless of the return of {@link #getResource} .
     *
     * @param index    The index to get the limit from.
     * @param resource The resource to get the limit for. If empty, this should return the theoretical limit of that index
     * @return The limit of the resource at the given index. Must be non-negative
     */
    @Nonnegative
    int getCapacity(@Nonnegative int index, T resource);

    @Nonnegative
    default long getCapacityAsLong(@Nonnegative int index, T resource) {
        return getCapacity(index, resource);
    }

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index. This is typically called in the {@link #insert(int, IResource, int, TransactionContext Context)} implementations or general resource querying.
     *
     * @param index    The index to check.
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean isValid(
            @Nonnegative int index,
            T resource);

    /**
     * Checks if the given index allows insertion of a resource, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <p>
     * Intended use is for something like a pipe graph lookup to be able to reduce the runtime workload on handlers that can never do a specific operation.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be inserted into the given index,
     * this should return true. To be clear, this value is assumed to be constant throughout the life-time of the handler and does <b>not</b> control the handler's logic in any way.
     * <h5>IMPORTANT:</h5>
     * Returning false, will not inherently prevent something from calling insert or change the result of that call,
     * so you will still need to handle those scenarios.
     * This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * well before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert.
     * <p>
     * If your handler can change size dynamically, then it may be wise to return true for this unless you know for certain a particular index would never be insertable to.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b> (hence the note about dynamic size erring on the side of caution)
     *
     * @param index The index to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean supportsInsertion(@Nonnegative int index);

    /**
     * Checks if the handler allows insertion into at least one index, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling insert or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b> (hence the note about dynamic size erring on the side of caution)
     *
     * @return True if a resource can be inserted, false otherwise.
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
     * Checks if the given index allows extraction of a resource, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be extracted from the given index,
     * this should return true.
     * <p>
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling extract or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract.
     * <p>
     * If your handler can change size dynamically, then it may be wise to return true for this unless you know for certain a particular index would never be extractable from.
     *
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b> (hence the note about dynamic size erring on the side of caution)
     *
     * @param index The index to check.
     * @return True if the resource can be extracted, false otherwise.
     */
    boolean supportsExtraction(@Nonnegative int index);

    /**
     * Checks if the handler allows extraction from at least one index, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling extract or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc.) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b> (hence the note about dynamic size erring on the side of caution)
     *
     * @return True if a resource can be extracted, false otherwise.
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
     * @param index       The index to insert the resource into.
     * @param resource    The resource to insert.
     * @param amount      The amount of the resource to insert. Must be non-negative
     * @param transaction Context The {@link TransactionContext Context } transaction to be inserting with.
     * @return The amount of the resource that was (or would have been, if simulated) inserted. Must be non-negative
     */
    @Nonnegative
    int insert(@Nonnegative int index, T resource, @Nonnegative int amount, TransactionContext transaction);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #insert(int, IResource, int, TransactionContext)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#insert(IResource, int, TransactionContext) ResourceStorage.insertBehaviour} for an example.
     *
     * @param resource    The resource to insert.
     * @param amount      The amount of the resource to insert. Must be non-negative
     * @param transaction The {@link TransactionContext } transaction to be inserting with.
     * @return The amount (Must be non-negative) of the resource that was (or would have been, if simulated) inserted.
     */
    int insert(T resource, @Nonnegative int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index       The index to extract the resource from.
     * @param resource    The resource to extract.
     * @param amount      The amount of the resource to extract. Must be non-negative
     * @param transaction The {@link TransactionContext } transaction to be extracting with.
     * @return The amount (Must be non-negative) of the resource that was (or would have been, if simulated) extracted.
     */
    @Nonnegative
    int extract(@Nonnegative int index, T resource, @Nonnegative int amount, TransactionContext transaction);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #extract(int, IResource, int, TransactionContext)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#extract(IResource, int, TransactionContext) ResourceStorage.extractBehaviour} for an example.
     *
     * @param resource    The resource to extract.
     * @param amount      The amount of the resource to extract. Must be non-negative
     * @param transaction The {@link TransactionContext } transaction to be extracting with.
     * @return The amount (Must be non-negative) of the resource that was (or would have been, if simulated) extracted.
     */
    @Nonnegative
    int extract(T resource, @Nonnegative int amount, TransactionContext transaction);

    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        //noinspection unchecked
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
