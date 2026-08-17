/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.extensions.IItemExtension;

/**
 * This event is fired on the forge bus before an Enderman detects that a player is looking at them.
 * It will not be fired if the detection is already prevented by {@link IItemExtension#isGazeDisguise}
 * <p>
 * This event is {@link ICancellableEvent}.
 * If this event is canceled, the Enderman will not target the player.
 */
public class EndermanAngerEvent extends LivingEvent implements ICancellableEvent {
    private final Player player;

    public EndermanAngerEvent(Enderman enderman, Player player) {
        super(enderman);
        this.player = player;
    }

    /**
     * The player that is being checked.
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public Enderman getEntity() {
        return (Enderman) super.getEntity();
    }
}
