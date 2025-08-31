/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.ItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class OneByOneItemContext implements ItemContext {
    private final ItemContext itemContext;

    public OneByOneItemContext(ItemContext itemContext) {
        this.itemContext = itemContext;
    }

    @Override
    public ItemResource getResource() {
        return itemContext.getResource();
    }

    @Override
    public int getAmount() {
        return Math.min(1, itemContext.getAmount());
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return itemContext.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return itemContext.extract(resource, 1, transaction);
    }
}
