/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.snapshots.NotificationSnapshot;

/**
 * Adapts any arbitrary resource handlers and wraps it into a IResourceContainer.
 * Note, this may have odd behaviour and will auto commit transactions. Use with caution.
 * <p>
 * To be more clear, any actions done during a transaction will not have a snapshot.
 */
public record ResourceHandlerToContainerAdapter<T extends IResource>(
        IResourceHandler<T> wrappedHandler,
        ResourceStack<T> defaultResource) implements IResourceContainer<T> {
    @Override
    public int size() {
        return wrappedHandler.size();
    }

    @Override
    public SnapshotJournal<?> getIndexJournal(int index) {
        return NotificationSnapshot.INSTANCE;
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
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            if (!resource.isEmpty())
                wrappedHandler.extract(index, resource, ResourceHandlerUtil.MAX, transaction);
            wrappedHandler.insert(index, stack.resource(), stack.amount(), transaction);
            transaction.commit();
        }
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
    public IResourceHandlerModifiable<T> asHandler() {
        return wrappedHandler instanceof IResourceHandlerModifiable<T> modifiable ? modifiable : IResourceContainer.super.asHandler();
    }
}
