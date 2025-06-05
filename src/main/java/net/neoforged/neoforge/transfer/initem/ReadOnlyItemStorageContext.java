/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.initem;

import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class ReadOnlyItemStorageContext implements InItemStorageContext {
    private final ItemVariant item;
    private final long amount;

    public ReadOnlyItemStorageContext(ItemVariant item, long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + amount);
        }
        this.item = item;
        this.amount = amount;
    }

    @Override
    public boolean supportsModification() {
        return false;
    }

    @Override
    public ItemVariant getCurrent() {
        return item;
    }

    @Override
    public long getCurrentAmount() {
        return amount;
    }

    @Override
    public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public String toString() {
        return "ReadOnlyItemStorageContext{" +
                "item=" + item +
                ", amount=" + amount +
                '}';
    }
}
