/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;

/**
 * Extension class for {@link ServerChunkCache}
 * <p>
 * This interface with its default methods allows for easy sending of payloads players watching a specific entity.
 * </p>
 */
@SuppressWarnings("resource")
public interface IServerChunkCacheExtension {
    default ServerChunkCache self() {
        return (ServerChunkCache) this;
    }

    /**
     * Sends a payload to all players watching the given entity.
     * <p>
     * If the entity is a player, the payload will be sent to that player.
     * </p>
     * 
     * @param entity  the entity that needs to be watched to receive the payload, and the player to send the payload to if the entity is a player.
     * @param payload the payload to send
     */
    default void broadcastAndSend(Entity entity, CustomPacketPayload payload) {
        self().broadcastAndSend(entity, new ClientboundCustomPayloadPacket(payload));
    }

    /**
     * Sends a payload to all players watching the given entity.
     * <p>
     * If the entity is a player, the payload will <bold>not</bold> be sent to that player.
     * </p>
     * 
     * @param entity  the entity that needs to be watched to receive the payload
     * @param payload the payload to send
     */
    default void broadcast(Entity entity, CustomPacketPayload payload) {
        self().broadcast(entity, new ClientboundCustomPayloadPacket(payload));
    }
}
