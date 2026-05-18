/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.entity.player.Player;

/**
 * The two subclasses of PlayerSwitchHotbarSlotEvent are fired when a player
 * switches the hotbar slot either by mouse scroll or keyboard input.
 *
 * @see PlayerSwitchHotbarSlotEvent.Client
 * @see PlayerSwitchHotbarSlotEvent.Server
 */
public abstract sealed class PlayerSwitchHotbarSlotEvent extends PlayerEvent {
    private final int oldSlotIndex;
    private final int newSlotIndex;

    public PlayerSwitchHotbarSlotEvent(Player player, int oldSlotIndex, int newSlotIndex) {
        super(player);
        this.oldSlotIndex = oldSlotIndex;
        this.newSlotIndex = newSlotIndex;
    }

    public int getOldSlotIndex() {
        return oldSlotIndex;
    }

    public int getNewSlotIndex() {
        return newSlotIndex;
    }

    /**
     * This event is fired when a player attempts to switch the hotbar slot on
     * the client side. If the packet is not successfully sent to the server
     * side, the {@link PlayerSwitchHotbarSlotEvent.Server} event will not be
     * fired after this event.
     */
    public static non-sealed class Client extends PlayerSwitchHotbarSlotEvent {
        public Client(Player player, int oldSlotIndex, int newSlotIndex) {
            super(player, oldSlotIndex, newSlotIndex);
        }
    }

    /**
     * This event is fired when the server side receives a packet of switching
     * the hotbar slot and the switching logic is actually performed.
     */
    public static non-sealed class Server extends PlayerSwitchHotbarSlotEvent {
        private final ServerGamePacketListener packetListener;

        public Server(ServerGamePacketListener packetListener, Player player, int oldSlotIndex, int newSlotIndex) {
            super(player, oldSlotIndex, newSlotIndex);
            this.packetListener = packetListener;
        }

        public ServerGamePacketListener getPacketListener() {
            return packetListener;
        }
    }
}
