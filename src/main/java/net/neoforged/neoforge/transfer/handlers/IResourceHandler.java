/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;

public interface IResourceHandler<T extends IResource> {
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
     * Gets the limit that index can hold of the given resource. If you'd like to get the theoretical limit of what
     * the index can hold, pass in a blank resource.
     *
     * @param index The index to get the limit from.
     * @param resource The resource to get the limit for.
     * @return The limit of the resource at the given index.
     */
    int getLimit(int index, T resource);

    /**
     * @param index The index to check.
     * @param resource The resource to check.
     * @return True if the resource can be inserted, false otherwise.
     */
    boolean isValid(int index, T resource);

    /**
     * @return True if the handler can insert resources, false otherwise.
     */
    boolean canInsert();

    /**
     * @return True if the handler can extract resources, false otherwise.
     */
    boolean canExtract();

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

    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
