/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An {@link IResourceHandler} that represents a handler that rejects all operations.
 * Note, if somehow it manages to get to {@link #getResource(int)} it will throw an error. We say "somehow" as the size is 0, so there should not be a valid path to get to that method when used correctly.
 * Use the {@link #instance()} method to safely cast to the resource type you are expecting. This should work for all resources.
 */
public final class EmptyResourceHandler<T extends IResource> implements IResourceHandler<T> {
    private final static EmptyResourceHandler<?> INSTANCE = new EmptyResourceHandler<>();

    public static <T extends IResource> EmptyResourceHandler<T> instance() {
        //noinspection unchecked
        return (EmptyResourceHandler<T>) INSTANCE;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int insert(T resource, int amount, TransactionContext transaction) {
        //No-op
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0;
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext transaction) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public int extract(T resource, int amount, TransactionContext transaction) {
        //No-op
        ResourceHandlerUtil.isEmpty(resource, amount);
        return 0;
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext transaction) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public T getResource(int index) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public int getAmount(int index) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public int getCapacity(int index, T resource) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public boolean isValid(int index, T resource) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public boolean supportsInsertion(int index) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public boolean supportsExtraction(int index) {
        throw new IllegalArgumentException("Invalid index: `" + index + "`Empty resource handlers are of size 0.");
    }

    @Override
    public String toString() {
        return "EmptyResourceHandler";
    }

    private EmptyResourceHandler() {}
}
