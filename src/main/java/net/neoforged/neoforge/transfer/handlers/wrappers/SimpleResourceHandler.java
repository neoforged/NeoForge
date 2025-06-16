/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;

/**
 * A simple resource handler that wraps an {@link IResourceHandler} and provides a simplified interface without
 * needing to manually start or stop a transaction.
 * <p>
 * <p>
 * This is intended for use in simple cases where you do not need the full power of the transaction system. It is important to remember, that this is not an IResourceHandler itself.
 *
 * @param <T> The type of resource this handler manages.
 */
public record SimpleResourceHandler<T extends IResource>(IResourceHandler<T> handler) {
    /**
     * An index in synonymous with "slot", "tank", "buffer", etc.
     *
     * @return The number of indices this handler manages. <strong>Must be Non-Negative</strong>
     */
    public int size() {
        return handler.size();
    }

    /**
     * @param index The index to get the resource from. <strong>Must be Non-Negative</strong>
     * @return The resource at the given index.
     */
    public T getResource(int index) {
        return handler.getResource(index);
    }

    /**
     * @param index The index to get the amount from. <strong>Must be Non-Negative</strong>
     * @return The amount of the resource at the given index. <strong>Must be Non-Negative</strong>
     */
    public int getAmount(int index) {
        return handler.getAmount(index);
    }

    /**
     * Gets the maximum capacity that the given index can handle of the given resource.
     * If an empty resource (an {@link IResource} that returns {@code true} on {@link IResource#isEmpty()}) is provided,
     * then the theoretical maximum should be returned, regardless of the return of {@link #getResource} .
     *
     * @param index    The index to get the limit from. <strong>Must be Non-Negative</strong>
     * @param resource The resource to get the limit for. If empty, this should return the theoretical limit of that index
     * @return The limit of the resource at the given index. <strong>Must be Non-Negative</strong>
     */
    public int getCapacity(int index, T resource) {
        return handler.getCapacity(index, resource);
    }

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index. This is typically called in the {@link #insert(int, IResource, int, TransferAction action)} implementations or general resource querying.
     *
     * @param index    The index to check. <strong>Must be Non-Negative</strong>
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    public boolean isValid(int index, T resource) {
        return handler.isValid(index, resource);
    }

    /**
     * Checks if the given index allows insertion of a resource, regardless of the state of the handler. Meaning this value should not be dynamic.
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
     * It is also advised to not use the result of this call in insert. Nor is it expected to be called before every insert.
     * <p>
     * If your handler can change size dynamically, then it may be wise to return true for this unless you know for certain a particular index would never be insertable to.
     * <p>
     * <b>This is expected to not change result unless it is accompanied by a capability invalidation.</b> (hence the note about dynamic size erring on the side of caution)
     *
     * @param index The index to check. <strong>Must be Non-Negative</strong>
     * @return True if any resource can be inserted at the specified index, false otherwise.
     */
    public boolean supportsInsertion(int index) {
        return handler.supportsInsertion(index);
    }

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
     * @return True if any resource can be inserted, false otherwise.
     */
    public boolean supportsInsertion() {
        return handler.supportsInsertion();
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
     * @param index The index to check. <strong>Must be Non-Negative</strong>
     * @return True if any resource can be extracted at the specified index, false otherwise.
     */
    public boolean supportsExtraction(int index) {
        return handler.supportsExtraction(index);
    }

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
     * @return True if any resource can be extracted, false otherwise.
     */
    public boolean supportsExtraction() {
        return handler.supportsExtraction();
    }

    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index      The index to insert the resource into. <strong>Must be Non-Negative</strong>
     * @param resource   The resource to insert.
     * @param amount     The amount of the resource to insert. <strong>Must be Non-Negative</strong>
     * @param actionType The action to take, whether to simulate the insertion or actually perform it.
     * @return The amount of the resource that was (or would have been, if simulated) inserted. <strong>Must be Non-Negative</strong>
     */
    public int insert(int index, T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            int inserted = handler.insert(index, resource, amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #insert(int, IResource, int, TransferAction)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#insert(IResource, int, TransactionContext) ResourceStorage.insertBehaviour} for an example.
     *
     * @param resource   The resource to insert.
     * @param amount     The amount of the resource to insert. <strong>Must be Non-Negative</strong>
     * @param actionType The action to take, whether to simulate the insertion or actually perform it.
     * @return The amount (A range from {@code 0} to {@code 2,147,483,647}) of the resource that was (or would have been, if simulated) inserted.
     */
    public int insert(T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            int inserted = handler.insert(resource, amount, transaction);
            actionType.commit(transaction);
            return inserted;
        }
    }

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index      The index to extract the resource from. <strong>Must be Non-Negative</strong>
     * @param resource   The resource to extract.
     * @param amount     The amount of the resource to extract. <strong>Must be Non-Negative</strong>
     * @param actionType The action to take, whether to simulate the insertion or actually perform it.
     * @return The amount (<strong>Must be Non-Negative</strong>) of the resource that was (or would have been, if simulated) extracted.
     */
    public int extract(int index, T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {

            int extracted = handler.extract(index, resource, amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #extract(int, IResource, int, TransferAction)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#extract(IResource, int, TransactionContext) ResourceStorage.extractBehaviour} for an example.
     *
     * @param resource   The resource to extract.
     * @param amount     The amount of the resource to extract. <strong>Must be Non-Negative</strong>
     * @param actionType The action to take, whether to simulate the insertion or actually perform it.
     * @return The amount (<strong>Must be Non-Negative</strong>) of the resource that was (or would have been, if simulated) extracted.
     */
    public int extract(T resource, int amount, TransferAction actionType) {
        try (Transaction transaction = TransactionManager.open(null)) {
            int extracted = handler.extract(resource, amount, transaction);
            actionType.commit(transaction);
            return extracted;
        }
    }
}
