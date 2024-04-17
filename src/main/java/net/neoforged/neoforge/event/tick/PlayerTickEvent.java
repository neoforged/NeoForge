/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two player tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class PlayerTickEvent extends PlayerEvent {
    protected PlayerTickEvent(Player player) {
        super(player);
    }

    /**
     * {@link PlayerTickEvent.Pre} is fired once per game tick, per player, before the player performs work for the current tick.
     * <p>
     * This event will fire on both the logical server and logical client, for all subclasses of {@link Player} on their respective sides.
     * <p>
     * As such, be sure to check {@link Level#isClientSide()} before performing any operations.
     */
    public static class Pre extends PlayerTickEvent {
        @ApiStatus.Internal
        public Pre(Player player) {
            super(player);
        }
    }

    /**
     * {@link PlayerTickEvent.Post} is fired once per game tick, per player, after the player performs work for the current tick.
     * <p>
     * This event will fire on both the logical server and logical client, for all subclasses of {@link Player} on their respective sides.
     * <p>
     * As such, be sure to check {@link Level#isClientSide()} before performing any operations.
     */
    public static class Post extends PlayerTickEvent {
        @ApiStatus.Internal
        public Post(Player player) {
            super(player);
        }
    }
}
