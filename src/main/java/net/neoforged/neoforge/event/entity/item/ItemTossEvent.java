/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Event that is fired whenever a player tosses (Q) an item or drag-n-drops a
 * stack of items outside the inventory GUI screens. Canceling the event will
 * stop the items from entering the world, but will not prevent them being
 * removed from the inventory - and thus removed from the system.
 */
public class ItemTossEvent extends ItemEvent implements ICancellableEvent {
    private final Player player;

    /**
     * Creates a new event for EntityItems tossed by a player.
     * 
     * @param entityItem The EntityItem being tossed.
     * @param player     The player tossing the item.
     */
    public ItemTossEvent(ItemEntity entityItem, Player player) {
        super(entityItem);
        this.player = player;
    }

    /**
     * The player tossing the item.
     */
    public Player getPlayer() {
        return player;
    }
}
