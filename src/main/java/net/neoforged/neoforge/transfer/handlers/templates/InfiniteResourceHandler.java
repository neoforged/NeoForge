/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * An {@link ISingleResourceHandler} that allows extraction of an unlimited amount of a specified resource.
 *
 * @param <T> The type of resource that this storage can accept.
 */
public class InfiniteResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    public T infinite;

    public InfiniteResourceHandler(T resource) {
        this.infinite = resource;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return 0; // doesn't allow insertions
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return amount;
    }

    @Override
    public T getResource(int index) {
        return infinite;
    }

    @Override
    public int getAmount(int ignoredIndex) {
        return ResourceHandlerUtil.PRETTY_MAX_INT; //This is mostly for pretty printing when displayed by mods.
    }

    @Override
    public int getCapacity(int index, T resource) {
        //0 or max? because technically it stores nothing nor would allow any.
        return ResourceHandlerUtil.PRETTY_MAX_INT; // Maximum capacity
    }

    @Override
    public int getCapacity(int ignoredIndex) {
        return ResourceHandlerUtil.PRETTY_MAX_INT; // Maximum capacity
    }

    @Override
    public boolean isValid(int index, T resource) {
        return resource.equals(infinite); // If the resource matches the current infinite
    }

    @Override
    public boolean allowsInsertion() {
        return false;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }
}
