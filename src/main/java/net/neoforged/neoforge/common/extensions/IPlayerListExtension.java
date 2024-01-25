/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Extension class for {@link PlayerList}
 * <p>
 * This interface with its default methods allows for easy sending of payloads to all, or specific, players on the server.
 * </p>
 */
public interface IPlayerListExtension {
    /**
     * {@return the PlayerList instance that this extension is attached to}
     */
    default PlayerList self() {
        return (PlayerList) this;
    }

    /**
     * Sends a payload to all players on the server
     *
     * @param payload the payload to send
     */
    default void broadcastAll(CustomPacketPayload payload) {
        self().broadcastAll(new ClientboundCustomPayloadPacket(payload));
    }

    /**
     * Sends a payload to all players within the specific level.
     *
     * @param payload     the payload to send
     * @param targetLevel the level to send the payload to.
     */
    default void broadcastAll(CustomPacketPayload payload, ResourceKey<Level> targetLevel) {
        self().broadcastAll(new ClientboundCustomPayloadPacket(payload), targetLevel);
    }

    /**
     * Sends a payload to all players within the specific level, within a given range around the target point
     *
     * @param x       the x coordinate of the target point
     * @param y       the y coordinate of the target point
     * @param z       the z coordinate of the target point
     * @param range   the range around the target point to send the payload to
     * @param level   the level to send the payload to
     * @param payload the payload to send
     */
    default void broadcast(
            double x, double y, double z, double range, ResourceKey<Level> level, CustomPacketPayload payload) {
        self().broadcast(null, x, y, z, range, level, new ClientboundCustomPayloadPacket(payload));
    }

    /**
     * Sends a payload to all players within the specific level, within a given range around the target point, excluding the specified player.
     *
     * @param excludedPlayer the player to exclude from the broadcast, when null all players will receive the payload.
     * @param x              the x coordinate of the target point
     * @param y              the y coordinate of the target point
     * @param z              the z coordinate of the target point
     * @param range          the range around the target point to send the payload to
     * @param level          the level to send the payload to
     * @param payload        the payload to send
     */
    default void broadcast(
            Player excludedPlayer, double x, double y, double z, double range, ResourceKey<Level> level, CustomPacketPayload payload) {
        self().broadcast(excludedPlayer, x, y, z, range, level, new ClientboundCustomPayloadPacket(payload));
    }
}
