/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.templates.PlayerContext;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.items.wrappers.PlayerInventoryHandler;

public class ItemHandlerHelper {

    /**
     * Inserts the given itemstack into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     * @param player The player to give the item to
     * @param stack The itemstack to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        PlayerInventoryHandler inventory = new PlayerInventoryHandler(player);
        inventory.insertOrDrop(ItemResource.of(stack), stack.getCount());
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (stack.isEmpty()) return;
        PlayerContext context = new PlayerContext(player, preferredSlot);
        context.insert(ItemResource.of(stack), stack.getCount(), TransferAction.EXECUTE);
    }
}
