/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.access;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

record OneByOneItemAccess(ItemAccess delegate) implements ItemAccess {
    @Override
    public ItemResource getResource() {
        return delegate.getResource();
    }

    @Override
    public int getAmount() {
        return Math.min(1, delegate.getAmount());
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.insert(resource, Math.min(amount, 1), transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return delegate.extract(resource, Math.min(amount, 1), transaction);
    }
}
