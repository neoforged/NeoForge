/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.tick;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Base class of the two player tick events.
 * <p>
 * These events are separate from {@link LivingTickEvent} due to the semantics of player ticks.
 * On the client, players tick from the usual {@link Entity#tick()} method, but on the server, they rely
 * on {@link ServerPlayer#doTick()} which is called from {@link ServerGamePacketListenerImpl#tick()}.
 * <p>
 * Use of these events should only be necessary if you rely on this specific timing.
 * 
 * @see PlayerTickEvent.Pre
 * @see PlayerTickEvent.Post
 */
public abstract class PlayerTickEvent extends PlayerEvent {
    protected PlayerTickEvent(Player player) {
        super(player);
    }

    /**
     * {@link PlayerTickEvent.Pre} is fired once per game tick, per player, before the player performs work for the current tick.
     * <p>
     * This event will fire on both the logical server and logical client, for subclasses of {@link Player} on their respective sides.
     * <p>
     * As such, be sure to check {@link Level#isClientSide()} before performing any operations.
     */
    public static class Pre extends PlayerTickEvent {
        public Pre(Player player) {
            super(player);
        }
    }

    /**
     * {@link PlayerTickEvent.Post} is fired once per game tick, per player, after the player performs work for the current tick.
     * <p>
     * This event will fire on both the logical server and logical client, for subclasses of {@link Player} on their respective sides.
     * <p>
     * As such, be sure to check {@link Level#isClientSide()} before performing any operations.
     */
    public static class Post extends PlayerTickEvent {
        public Post(Player player) {
            super(player);
        }
    }
}
