/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;

/**
 * @deprecated Use {@link ItemUtil} instead
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class ItemHandlerHelper {
    /**
     * Inserts the given itemstack into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Use {@link ItemUtil#giveItemToPlayer(Player, ItemStack)} instead
     */
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        ItemUtil.giveItemToPlayer(player, stack);
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Use {@link ItemUtil#giveItemToPlayer(Player, ItemStack, int)} instead
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        ItemUtil.giveItemToPlayer(player, stack, preferredSlot);
    }
}
