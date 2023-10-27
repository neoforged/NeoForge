/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>Fired after the player's movement inputs are updated.</p>
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain Event.HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class MovementInputUpdateEvent extends PlayerEvent
{
    private final Input input;

    @ApiStatus.Internal
    public MovementInputUpdateEvent(Player player, Input input)
    {
        super(player);
        this.input = input;
    }

    /**
     * {@return the player's movement inputs}
     */
    public Input getInput()
    {
        return input;
    }
}
