/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.initem;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.item.InventoryStorage;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class PlayerStorageContext implements InItemStorageContext {
    private final InventoryStorage inventoryStorage;
    private final Storage<ItemVariant> slot;

    PlayerStorageContext(Player player, Storage<ItemVariant> slot) {
        this.inventoryStorage = InventoryStorage.of(player);
        this.slot = slot;
    }

    @Override
    public ItemVariant getCurrent() {
        return slot.getResource(0);
    }

    @Override
    public long getCurrentAmount() {
        return slot.getAmount(0);
    }

    @Override
    public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        long inserted = slot.insert(itemVariant, maxAmount, transaction);
        if (inserted < maxAmount) {
            inventoryStorage.placeItemBackInInventory(itemVariant, maxAmount, transaction);
        }
        // Any leftover is dropped, so the max amount can always be accepted
        return maxAmount;
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return slot.extract(itemVariant, maxAmount, transaction);
    }
}
