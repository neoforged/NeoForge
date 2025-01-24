/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;

/**
 * An {@link ISingleResourceHandler} that allows extraction of an unlimited amount of a specified resource.
 *
 * @param <T> The type of resource that this storage can accept.
 */
public class InfiniteResourceHandler<T extends IResource> implements IResourceHandler<T> {
    public T infinite;

    public InfiniteResourceHandler(T resource) {
        this.infinite = resource;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        return 0; // doesn't allow insertions
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return 0; // doesn't allow insertions
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        return resource.equals(infinite) ? amount : 0;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return extract(0, resource, amount, action);
    }

    @Override
    public T getResource(int index) {
        return infinite;
    }

    @Override
    public int getAmount(int index) {
        return 2000000000; //This is mostly for pretty printing when displayed by mods.
    }

    @Override
    public int getCapacity(int index, T resource) {
        //0 or max? because technically it stores nothing nor would allow any.
        return ResourceHandlerUtil.PRETTY_MAX_INT; // Maximum capacity
    }

    @Override
    public int getCapacity(int index) {
        return ResourceHandlerUtil.PRETTY_MAX_INT; // Maximum capacity
    }

    @Override
    public boolean isValid(T resource) {
        return resource.equals(infinite); // If the resource matches the current infinite
    }

    @Override
    public boolean isValid(int index, T resource) {
        return isValid(resource); // If the resource matches the current infinite
    }

    @Override
    public boolean allowsInsertion(int index) {
        return false; // Never insertable
    }

    @Override
    public boolean allowsExtraction(int index) {
        return true; // Always extractable
    }
}
