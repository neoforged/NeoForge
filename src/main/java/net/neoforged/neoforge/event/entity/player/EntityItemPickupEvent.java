/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is called when a player collides with a EntityItem on the ground.
 * The event can be canceled, and no further processing will be done.
 *
 * You can set the result of this event to ALLOW which will trigger the
 * processing of achievements, FML's event, play the sound, and kill the
 * entity if all the items are picked up.
 *
 * setResult(ALLOW) is the same as the old setHandled()
 */
@Event.HasResult
public class EntityItemPickupEvent extends PlayerEvent implements ICancellableEvent {
    private final ItemEntity item;

    public EntityItemPickupEvent(Player player, ItemEntity item) {
        super(player);
        this.item = item;
    }

    public ItemEntity getItem() {
        return item;
    }
}
