/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.access;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class PlayerItemAccess implements ItemAccess {
    private final PlayerInventoryWrapper inventoryWrapper;
    private final ResourceHandler<ItemResource> slot;

    public PlayerItemAccess(PlayerInventoryWrapper inventoryWrapper, ResourceHandler<ItemResource> slot) {
        this.inventoryWrapper = inventoryWrapper;
        this.slot = slot;
    }

    @Override
    public ItemResource getResource() {
        return slot.getResource(0);
    }

    @Override
    public int getAmount() {
        return slot.getAmountAsInt(0);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        int inserted = slot.insert(0, resource, amount, transaction);
        if (amount > inserted) {
            inventoryWrapper.placeItemBackInInventory(resource, amount - inserted, transaction);
        }
        // Any leftover is dropped, so the full amount can always be accepted
        return amount;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return slot.extract(0, resource, amount, transaction);
    }
}
