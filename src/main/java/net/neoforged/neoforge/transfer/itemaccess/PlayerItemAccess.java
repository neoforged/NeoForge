/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.itemaccess;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

class PlayerItemAccess implements ItemAccess {
    protected final PlayerInventoryWrapper handler;
    protected final int index;

    public PlayerItemAccess(Player player, int index) {
        this.handler = PlayerInventoryWrapper.of(player);
        Objects.checkIndex(index, handler.size());
        this.index = index;
    }

    @Override
    public ItemResource getResource() {
        return handler.getResource(index);
    }

    @Override
    public int getAmount() {
        return handler.getAmountAsInt(index);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        int inserted = handler.insert(index, resource, amount, transaction);
        if (amount > inserted) {
            handler.placeItemBackInInventory(resource, amount - inserted, transaction);
        }

        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return handler.extract(index, resource, amount, transaction);
    }
}
