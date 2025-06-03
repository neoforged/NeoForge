/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class OneByOneItemContext implements IItemContext {
    private final IItemContext itemContext;

    public OneByOneItemContext(IItemContext itemContext) {
        this.itemContext = itemContext;
    }

    @Override
    public ItemResource getResource() {
        return itemContext.getResource();
    }

    @Override
    public int getAmount() {
        return Math.max(1, itemContext.getAmount());
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isInvalidInquiry(resource, amount)) return 0;
        return itemContext.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isInvalidInquiry(resource, amount)) return 0;
        return itemContext.extract(resource, amount, transaction);
    }
}
