/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ReadOnlyItemContext(IItemContext context) implements IItemContext {
    @Override
    public ItemResource getResource() {
        return context.getResource();
    }

    @Override
    public int getAmount() {
        return context.getAmount();
    }

    @Override
    public int insert(ItemResource itemVariant, int amount, TransactionContext transaction) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            return context.insert(itemVariant, amount, subTransaction);
        }
    }

    @Override
    public int extract(ItemResource itemVariant, int amount, TransactionContext transaction) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            return context.extract(itemVariant, amount, subTransaction);
        }
    }

    @Override
    public int exchange(ItemResource resource, int amount, TransactionContext transaction) {
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            return context.exchange(resource, amount, subTransaction);
        }
    }
}
