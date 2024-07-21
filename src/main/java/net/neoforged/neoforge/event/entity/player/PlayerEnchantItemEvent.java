/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * This event captures when a player action results in an item being enchanted.  If a single action results in
 * multiple {@link EnchantmentInstance}s being applied, this event should be posted for each one.
 */
public class PlayerEnchantItemEvent extends PlayerEvent {
    private final ItemStack enchantedItem;
    private final EnchantmentInstance enchantment;

    public PlayerEnchantItemEvent(Player player, ItemStack enchantedItem, EnchantmentInstance enchantment) {
        super(player);
        this.enchantedItem = enchantedItem;
        this.enchantment = enchantment;
    }

    /**{@return the {@link ItemStack} being enchanted}*/
    public ItemStack getEnchantedItem() {
        return enchantedItem;
    }
    /**{@return the {@link EnchantmentInstance} being applied to the item for this event firing}*/
    public EnchantmentInstance getEnchantment() {
        return enchantment;
    }
}
