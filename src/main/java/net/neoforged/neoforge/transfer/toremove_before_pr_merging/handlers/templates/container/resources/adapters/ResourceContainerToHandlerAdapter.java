/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.adapters;

import java.util.Objects;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.IResourceContainer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A basic {@link IResourceHandlerModifiable} implementation derived from an {@link IResourceContainer}.
 */
public record ResourceContainerToHandlerAdapter<TResource extends IResource>(
        IResourceContainer<TResource> container,
        IHandleIOBehaviour behavior) implements IResourceHandlerModifiable<TResource> {
    @Override
    public int size() {
        return container.size();
    }

    @Override
    public boolean supportsInsertion(int index) {
        Objects.checkIndex(index, size());
        return behavior.canInsert(index);
    }

    @Override
    public boolean supportsExtraction(int index) {
        Objects.checkIndex(index, size());
        return behavior.canExtract(index);
    }

    @Override
    public TResource getResource(int index) {
        Objects.checkIndex(index, size());
        return container.get(index).resource();
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return container.get(index).amount();
    }

    @Override
    public int getCapacity(int index, TResource resource) {
        Objects.checkIndex(index, size());
        return container.getCapacity(index, resource);
    }

    @Override
    public boolean isValid(int index, TResource resource) {
        return behavior.canInsert(index) && container.isValid(index, resource);
    }

    @Override
    public int insert(TResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        var handled = 0;
        var size = size();
        for (var index = 0; index < size; index++) {
            handled += insertBehaviour(index, resource, amount - handled, context);
            if (handled == amount)
                break;
        }
        return handled;
    }

    @Override
    public int insert(int index, TResource resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return insertBehaviour(index, resource, amount, context);
    }

    private int insertBehaviour(int index, TResource resource, int amount, TransactionContext transaction) {
        if (!behavior.canInsert(index) || !container.isValid(index, resource)) return 0;

        var resourceStackInSlot = container.get(index);
        var capacity = container.getCapacity(index, resource);

        int inserted, newStackSize;
        if (resourceStackInSlot.isEmpty()) {
            //the slot is empty
            inserted = Math.min(capacity, amount);
            newStackSize = inserted;
        } else {
            //is there an item in the slot already?
            if (!resourceStackInSlot.resource().equals(resource)) return 0;

            inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
            newStackSize = resourceStackInSlot.amount() + inserted;
        }

        if (newStackSize > 0) {
            container.getIndexJournal(index).updateSnapshots(transaction);
            set(index, resource, newStackSize);
        }

        return inserted;
    }

    @Override
    public int extract(int index, TResource resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return extractBehaviour(index, resource, amount, context);
    }

    @Override
    public int extract(TResource resource, int amount, TransactionContext context) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        var handled = 0;
        var size = container.size();
        for (var index = 0; index < size; index++) {
            handled += extractBehaviour(index, resource, amount - handled, context);
            if (handled == amount) break;
        }
        return handled;
    }

    private int extractBehaviour(int index, TResource resource, int amount, TransactionContext transaction) {
        if (!behavior.canExtract(index)) return 0;

        var currentStack = container.get(index);
        if (!resource.equals(currentStack.resource())) return 0;

        var currentAmount = currentStack.amount();
        int handledAmount = Math.min(amount, currentAmount);

        container.getIndexJournal(index).updateSnapshots(transaction);
        set(index, resource, currentAmount - handledAmount);
        return handledAmount;
    }

    @Override
    public void set(int index, TResource resource, int amount) {
        //Blind trust that index, resource, and amount are all valid in some way.
        var current = container.get(index);
        if (ResourceHandlerUtil.isEmpty(resource, amount))
            container.set(index, container.defaultResource().mutable());
        else if (current.resource().equals(resource))
            container.set(index, current.withAmount(amount));
        else
            container.set(index, MutableResourceStack.of(resource, amount));
    }
}
