/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.InventoryWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class CreativePlayerContext extends StaticContext {
    protected final InventoryWrapper handler;

    public CreativePlayerContext(ItemResource resource, int amount, Player player) {
        super(resource, amount);
        this.handler = InventoryWrapper.of(player);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isInvalidInquiry(resource, amount)) return 0;

        boolean isMissing;
        try (var testTransaction = Transaction.open(transaction)) {
            //simulates the action, makes use of the snapshot to revert
            isMissing = handler.extract(resource, 1, testTransaction) == 0;
        }

        if (isMissing) return handler.insert(resource, 1, transaction);

        return amount;
    }
}
