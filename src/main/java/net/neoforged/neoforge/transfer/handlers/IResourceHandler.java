/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.templates.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import org.jetbrains.annotations.Range;

/**
 * A generic handler for handling a resource of type {@link T}.
 * @param <T> The type of resource this handler manages.
 */
public interface IResourceHandler<T extends IResource> {
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
    T getResource(int index);

    /**
     * @param index The index to get the amount from.
     * @return The amount of the resource at the given index.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int getAmount(int index);


    /**
     * Gets the theoretical maximum amount that the given index can hold of "any" resource. If there is something in the slot, it is valid to use its max bounds.
     *
     * @param index The index to get the limit from.
     * @return The limit of the resource at the given index.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int getCapacity(int index);

    /**
     * Gets the maximum amount that the given index can have of the given resource. If your capacity is constant, no matter
     * the resource, you can just return the result of {@link #getCapacity(int)}. This is historically the case for fluids,
     * but not for items.
     *
     * @param index The index to get the limit from.
     * @param resource The resource to get the limit for.
     * @return The limit of the resource at the given index.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int getCapacity(int index, T resource);

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index.
     *
     * @param index The index to check.
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean isValid(int index, T resource);

    /**
     * Checks if the given index allows insertion of a resource, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <p>
     * Intended use is for something like a pipe graph lookup to be able to reduce the runtime workload on handlers that can never do a specific operation.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be inserted into the given index,
     * this should return true. To be clear, this value assumed to be constant throughout the life-time of the handler and does <b>not</b> control the handler's logic in any way.
     * <h5>IMPORTANT:</h5>
     * This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling insert or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc) to be able to infer what it can do with the handler
     * well before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert if the lookup is complex.
     *
     * @param index The index to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean allowsInsertion(int index);

    /**
     * Checks if the handler allows insertion into at least one index, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling insert or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in insert if the lookup is complex.
     *
     * @return True if a resource can be inserted, false otherwise.
     */
    default boolean allowsInsertion() {
        for (int i = 0; i < size(); i++) {
            if (allowsInsertion(i)) {
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
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract if the lookup is complex.
     *
     * @param index The index to check.
     * @return True if the resource can be extracted, false otherwise.
     */
    boolean allowsExtraction(int index);

    /**
     * Checks if the handler allows extraction from at least one index, regardless of the state of the handler. Also meaning this value is non-dynamic.
     * <h5>IMPORTANT:</h5> This does not control your handler's logic in any way. Returning false, will not inherently prevent something from calling extract or change the result of that call,
     * so you will still need to handle those scenarios. This is to allow things like logistics (pipes, searches, etc) to be able to infer what it can do with the handler
     * before actually operating.
     * <p>
     * It is also advised to not use the result of this call in extract if the lookup is complex.
     *
     * @return True if a resource can be extracted, false otherwise.
     */
    default boolean allowsExtraction() {
        for (int i = 0; i < size(); i++) {
            if (allowsExtraction(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index    The index to insert the resource into.
     * @param resource The resource to insert.
     * @param amount   The amount of the resource to insert.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int insert(int index, T resource, @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int amount, TransferAction action);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #insert(int, IResource, int, TransferAction)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#insert(IResource, int, TransferAction) ResourceStorage.insert} for an example.
     *
     * @param resource The resource to insert.
     * @param amount   The amount of the resource to insert.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int insert(T resource, @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int amount, TransferAction action);

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index    The index to extract the resource from.
     * @param resource The resource to extract.
     * @param amount   The amount of the resource to extract.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int extract(int index, T resource, @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int amount, TransferAction action);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     * <p>
     * Implementation advice, don't just have this call {@link #extract(int, IResource, int, TransferAction)}, as you may needlessly re-check validations.
     * See {@link ResourceStorageHandler#extract(IResource, int, TransferAction) ResourceStorage.extract} for an example.
     *
     *
     * @param resource The resource to extract.
     * @param amount   The amount of the resource to extract.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT)
    int extract(T resource, @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int amount, TransferAction action);

    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
