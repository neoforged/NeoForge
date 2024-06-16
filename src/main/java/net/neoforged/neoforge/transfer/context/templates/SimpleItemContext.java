/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.context.templates;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.ScopedHandlerWrapper;
import net.neoforged.neoforge.transfer.items.ItemResource;

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
        this(mainHandler, ScopedHandlerWrapper.fromHandlerExcludingIndices(mainHandler, new int[]{index}), index);
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
    public int insert(ItemResource resource, int amount, TransferAction action) {
        int inserted = mainHandler.insert(index, resource, amount, action);
        if (inserted < amount) {
            return insertOverflow(resource, amount - inserted, action);
        }
        return inserted;
    }

    public int insertOverflow(ItemResource resource, int amount, TransferAction action) {
        return overflowHandler.insert(resource, amount, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return mainHandler.extract(index, resource, amount, action);
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransferAction action) {
        int currentAmount = getAmount();
        if (amount >= currentAmount) {
            if (action.isExecuting()) {
                mainHandler.set(index, resource, currentAmount);
            }
            return currentAmount;
        }
        int extracted = extract(getResource(), amount, action);
        if (extracted > 0) {
            return insertOverflow(resource, extracted, action);
        }
        return 0;
    }
}
