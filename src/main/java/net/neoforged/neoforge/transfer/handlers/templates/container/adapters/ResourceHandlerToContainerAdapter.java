/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

/**
 * Adapts any arbitrary resource handlers and wraps it into a IResourceContainer. Note, this may have odd behaviour when dealing with other mod's handlers here.
 */
public record ResourceHandlerToContainerAdapter<T extends IResource>(
        IResourceHandler<T> wrappedHandler, ResourceStack<T> emptyResource) implements IResourceContainer<T> {
    @Override
    public int size() {
        return wrappedHandler.size();
    }

    @Override
    public MutableResourceStack<T> get(int index) {
        var resource = wrappedHandler.getResource(index);
        if (resource.isEmpty()) return defaultResource().mutable();
        return MutableResourceStack.of(resource, wrappedHandler.getAmount(index));
    }

    @Override
    public void set(int index, MutableResourceStack<T> stack) {
        if (wrappedHandler instanceof IResourceHandlerModifiable<T> modifiable) {
            modifiable.set(index, stack.resource(), stack.amount());
            return;
        }

        var resource = wrappedHandler.getResource(index);
        if (!resource.isEmpty())
            wrappedHandler.extract(index, resource, ResourceHandlerUtil.PRETTY_MAX_INT, TransferAction.EXECUTE);
        wrappedHandler.insert(index, stack.resource(), stack.amount(), TransferAction.EXECUTE);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return wrappedHandler.isValid(index, resource);
    }

    @Override
    public int getCapacity(int index, T resource) {
        return wrappedHandler.getCapacity(index, resource);
    }

    @Override
    public int getCapacity(int index) {
        return wrappedHandler.getCapacity(index);
    }

    @Override
    public IResourceHandlerModifiable<T> asHandler() {
        return wrappedHandler instanceof IResourceHandlerModifiable<T> modifiable ? modifiable : IResourceContainer.super.asHandler();
    }

    @Override
    public ResourceStack<T> defaultResource() {
        return emptyResource();
    }
}
