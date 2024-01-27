/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.brewing;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * This event is called when a player picks up a potion from a brewing stand.
 */
public class PlayerBrewedPotionEvent extends PlayerEvent {
    private final ItemStack stack;

    public PlayerBrewedPotionEvent(Player player, ItemStack stack) {
        super(player);
        this.stack = stack;
    }

    /**
     * The ItemStack of the potion.
     */
    public ItemStack getStack() {
        return stack;
    }
}
