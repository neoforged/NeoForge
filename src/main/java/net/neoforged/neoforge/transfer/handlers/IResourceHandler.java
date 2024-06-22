/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

/**
 * A generic handler for handling a resource of type {@link T}.
 * @param <T> The type of resource this handler manages.
 */
public interface IResourceHandler<T extends IResource> {
    /**
     * Inserts a given amount of the resource into the handler at the given index.
     *
     * @param index The index to insert the resource into.
     * @param resource The resource to insert.
     * @param amount The amount of the resource to insert.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    int insert(int index, T resource, int amount, TransferAction action);

    /**
     * Extracts a given amount of the resource from the handler at the given index.
     *
     * @param index The index to extract the resource from.
     * @param resource The resource to extract.
     * @param amount The amount of the resource to extract.
     * @param action   The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    int extract(int index, T resource, int amount, TransferAction action);

    /**
     * Inserts a given amount of the resource into the handler. Distribution of the resource is up to the handler.
     *
     * @param resource The resource to insert.
     * @param amount The amount of the resource to insert.
     * @param action  The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    int insert(T resource, int amount, TransferAction action);

    /**
     * Extracts a given amount of the resource from the handler. Distribution of the resource is up to the handler.
     *
     * @param resource The resource to extract.
     * @param amount The amount of the resource to extract.
     * @param action  The kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    int extract(T resource, int amount, TransferAction action);

    /**
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
    int getAmount(int index);

    /**
     * Gets the maximum amount that the given index can have of the given resource. If your capacity is constant, no matter
     * the resource, you can just return the result of {@link #getCapacity(int)}. This is historically the case for fluids,
     * but not for items.
     *
     * @param index The index to get the limit from.
     * @param resource The resource to get the limit for.
     * @return The limit of the resource at the given index.
     */
    int getCapacity(int index, T resource);

    /**
     * Gets the theoretical maximum amount that the given index can hold of a resource.
     *
     * @param index The index to get the limit from.
     * @return The limit of the resource at the given index.
     */
    int getCapacity(int index);

    /**
     * Checks if the given resource is allowed to be inserted into the handler at the given index, regardless of the
     * current state of the handler.
     *
     * @param index The index to check.
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean isValid(int index, T resource);

    /**
     * Checks if the given index allows insertion of a resource, regardless of the state of the handler.
     *
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be inserted into the given index,
     * this should return true.
     *
     * @param index The index to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean allowsInsertion(int index);

    /**
     * Checks if the given index allows extraction of a resource, regardless of the state of the handler.
     * <p>
     * As long as the handler could, under the right conditions, allow a resource to be extracted from the given index,
     * this should return true.
     *
     * @param index The index to check.
     * @return True if the resource can be extracted, false otherwise.
     */
    boolean allowsExtraction(int index);

    /**
     * Checks if the handler allows insertion into at least one index, regardless of the state of the handler.
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
     * Checks if the handler allows extraction from at least one index, regardless of the state of the handler.
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

    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
