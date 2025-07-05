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

/**
 * A special context for creative players to only allow one of an item to be inserted if they don't already have it,
 * otherwise it is ignored.
 */
public class CreativePlayerItemContext extends StaticItemContext {
    protected final PlayerInventoryWrapper handler;

    public CreativePlayerItemContext(ItemResource resource, int amount, Player player) {
        super(resource, amount);
        this.handler = PlayerInventoryWrapper.of(player);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        if (!ResourceHandlerUtil.contains(handler, resource)) {
            //if the resource was not in the creative player's inventory, we will give them one;
            handler.insert(resource, 1, transaction);
        }
        return amount;
    }
}
