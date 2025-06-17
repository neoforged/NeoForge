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

public class CreativePlayerItemContext extends StaticItemContext {
    protected final PlayerInventoryWrapper handler;

    public CreativePlayerItemContext(ItemResource resource, int amount, Player player) {
        super(resource, amount);
        this.handler = PlayerInventoryWrapper.of(player);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        boolean wasFound = ResourceHandlerUtil.contains(handler, resource);
        return wasFound ? amount : handler.insert(resource, 1, transaction);
    }
}
