/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;

public class CreativePlayerItemContext extends StaticItemContext {
    protected final PlayerInventoryWrapper handler;

    public CreativePlayerItemContext(ItemResource resource, int amount, Player player) {
        super(resource, amount);
        this.handler = PlayerInventoryWrapper.of(player);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        boolean isMissing;
        try (var testTransaction = TransactionManager.open(transaction)) {
            //simulates the action, makes use of the snapshot to revert
            isMissing = handler.extract(resource, 1, testTransaction) == 0;
        }

        if (isMissing) return handler.insert(resource, 1, transaction);

        return amount;
    }
}
