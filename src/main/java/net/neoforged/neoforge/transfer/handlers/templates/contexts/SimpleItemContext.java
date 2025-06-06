/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.ScopedResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A simple implementation of {@link IItemContext} that delegates to a main handler and an overflow handler.
 * <br>
 * The main handler is used for the main item, while the overflow handler is used for any extra items. You can optionally
 * provide an index to exclude from the overflow handler, and a scoped handler will be created to exclude that index for
 * overflow operations.
 */
public class SimpleItemContext implements IItemContext {
    protected final IResourceHandlerModifiable<ItemResource> mainHandler;
    protected final IResourceHandler<ItemResource> overflowHandler;
    protected final int index;

    public SimpleItemContext(IResourceHandlerModifiable<ItemResource> mainHandler, IResourceHandler<ItemResource> overflowHandler, int index) {
        this.mainHandler = mainHandler;
        this.overflowHandler = overflowHandler;
        this.index = index;
    }

    public SimpleItemContext(IResourceHandlerModifiable<ItemResource> mainHandler, int index) {
        this(mainHandler, ScopedResourceHandler.fromHandlerExcludingIndices(mainHandler, new int[] { index }), index);
    }

    @Override
    public ItemResource getResource() {
        return mainHandler.getResource(index);
    }

    @Override
    public int getAmount() {
        return mainHandler.getAmount(index);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        int inserted = mainHandler.insert(index, resource, amount, transaction);
        if (inserted < amount) {
            return insertOverflow(resource, amount - inserted, transaction);
        }
        return inserted;
    }

    public int insertOverflow(ItemResource resource, int amount, TransactionContext transaction) {
        return overflowHandler.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return mainHandler.extract(index, resource, amount, transaction);
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransactionContext transaction) {
        int currentAmount = getAmount();
        if (amount >= currentAmount) {
            //snapshot is handled by the handler itself
            mainHandler.set(index, resource, currentAmount);
            return currentAmount;
        }
        int extracted = extract(getResource(), amount, transaction);
        if (extracted > 0) {
            return insertOverflow(resource, extracted, transaction);
        }
        return 0;
    }
}
