/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class IndexItemContext implements IItemContext {
    /**
     * Creates a context object for working with resource handler contained in an item.
     *
     * @param handler The handler containing the item.
     * @param index   The index in {@code handler}, where the item can be found.
     */
    public static IndexItemContext of(IResourceHandler<ItemResource> handler, int index) {
        return new IndexItemContext(handler, index, true);
    }

    /**
     * Creates a context object for working with resource handler contained in an item at a specific index of a resource handler.
     *
     * @param handler The handler containing the item.
     * @param index   The index in {@code handler}, where the item can be found and only allow mutations on that {@code index}.
     */
    public static IndexItemContext ofSpecific(IResourceHandler<ItemResource> handler, int index) {
        return new IndexItemContext(handler, index, false);
    }

    private final int index;
    private final boolean allowsOverflow;
    private final IResourceHandler<ItemResource> handler;

    private IndexItemContext(IResourceHandler<ItemResource> handler, int index, boolean allowsOverflow) {
        this.handler = handler;
        this.index = index;
        this.allowsOverflow = allowsOverflow;
    }

    @Override
    public ItemResource getResource() {
        return index < handler.size() ? handler.getResource(index) : ItemResource.EMPTY;
    }

    @Override
    public int getAmount() {
        return index < handler.size() ? handler.getAmount(index) : 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        int inserted = handler.insert(index, resource, amount, transaction);
        if (allowsOverflow && inserted < amount) {
            inserted += handler.insert(resource, amount - inserted, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(ItemResource itemVariant, int amount, TransactionContext transaction) {
        return handler.extract(index, itemVariant, amount, transaction);
    }
}
