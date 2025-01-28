/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryHandler;

/**
 * @deprecated Moved to {@link net.neoforged.neoforge.transfer.ResourceHandlerUtil ResourceHandlerUtil}
 */
@Deprecated(forRemoval = true, since = "1.21.4")
public class ItemHandlerHelper {

    /**
     * Inserts the given itemstack into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Moved to {@link net.neoforged.neoforge.transfer.ResourceHandlerUtil ResourceHandlerUtil}
     */
    @Deprecated(forRemoval = true, since = "1.21.4")
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
     * @deprecated Moved to {@link net.neoforged.neoforge.transfer.ResourceHandlerUtil ResourceHandlerUtil}
     */
    @Deprecated(forRemoval = true, since = "1.21.4")
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (stack.isEmpty()) return;
        PlayerContext context = new PlayerContext(player, preferredSlot);
        context.insert(ItemResource.of(stack), stack.getCount(), TransferAction.EXECUTE);
    }
}
