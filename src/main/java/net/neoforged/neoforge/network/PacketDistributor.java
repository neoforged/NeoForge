/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Means to distribute packets in various ways
 */
public final class PacketDistributor {
    private PacketDistributor() {}

    /**
     * Send the given payload(s) to the server
     */
    public static void sendToServer(CustomPacketPayload... payloads) {
        ClientPacketListener listener = Objects.requireNonNull(Minecraft.getInstance().getConnection());
        for (CustomPacketPayload payload : payloads) {
            listener.send(payload);
        }
    }

    /**
     * Send the given payload(s) to the given player
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload... payloads) {
        player.connection.send(makeClientboundPacket(payloads));
    }

    /**
     * Send the given payload(s) to all players in the given dimension
     */
    public static void sendToPlayersInDimension(ResourceKey<Level> dimension, CustomPacketPayload... payloads) {
        getServer().getPlayerList().broadcastAll(makeClientboundPacket(payloads), dimension);
    }

    /**
     * Send the given payload(s) to all players near the given {@link TargetPoint}
     */
    public static void sendToPlayersNear(TargetPoint target, CustomPacketPayload... payloads) {
        Packet<?> packet = makeClientboundPacket(payloads);
        getServer().getPlayerList().broadcast(target.excluded, target.x, target.y, target.z, target.r, target.dim, packet);
    }

    /**
     * Send the given payload(s) to all players on the server
     */
    public static void sendToAllPlayers(CustomPacketPayload... payloads) {
        getServer().getPlayerList().broadcastAll(makeClientboundPacket(payloads));
    }

    /**
     * Send the given payload(s) to all players tracking the given entity
     */
    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload... payloads) {
        ((ServerChunkCache) entity.level().getChunkSource()).broadcast(entity, makeClientboundPacket(payloads));
    }

    /**
     * Send the given payload(s) to all players tracking the given entity and the entity itself if it is a player
     */
    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload... payloads) {
        ((ServerChunkCache) entity.level().getChunkSource()).broadcastAndSend(entity, makeClientboundPacket(payloads));
    }

    /**
     * Send the given payload(s) to all players tracking the chunk at the given position in the given level
     */
    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload... payloads) {
        Packet<?> packet = makeClientboundPacket(payloads);
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            player.connection.send(packet);
        }
    }

    private static Packet<?> makeClientboundPacket(CustomPacketPayload... payloads) {
        if (payloads.length > 1) {
            final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            for (CustomPacketPayload payload : payloads) {
                packets.add(new ClientboundCustomPayloadPacket(payload));
            }
            return new ClientboundBundlePacket(packets);
        } else if (payloads.length == 1) {
            return new ClientboundCustomPayloadPacket(payloads[0]);
        } else {
            throw new IllegalArgumentException("Must provide at least one payload");
        }
    }

    /**
     * A target point with excluded entity
     *
     * @param excluded Entity to exclude
     * @param x        X coordinate
     * @param y        Y coordinate
     * @param z        Z coordinate
     * @param r        Radius
     * @param dim      Target dimension
     */
    public record TargetPoint(@Nullable ServerPlayer excluded, double x, double y, double z, double r, ResourceKey<Level> dim) {
        /**
         * A target point without excluded entity
         *
         * @param x   X coordinate
         * @param y   Y coordinate
         * @param z   Z coordinate
         * @param r   Radius
         * @param dim Target dimension
         */
        public TargetPoint(double x, double y, double z, double r, ResourceKey<Level> dim) {
            this(null, x, y, z, r , dim);
        }
    }

    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
