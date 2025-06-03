/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ItemUtil;

/**
 * @deprecated Moved to {@link ItemUtil}
 */
@Deprecated(forRemoval = true, since = "1.21.6")
public class ItemHandlerHelper {
    /**
     * Inserts the given itemstack into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Moved to {@link ItemUtil}
     */
    @Deprecated(forRemoval = true, since = "1.21.6")
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        ItemUtil.giveItemToPlayer(player, stack);
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Moved to {@link ItemUtil}
     */
    @Deprecated(forRemoval = true, since = "1.21.6")
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        ItemUtil.giveItemToPlayer(player, stack, preferredSlot);
    }
}
