/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import com.google.common.base.Preconditions;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.fml.loading.FMLEnvironment;
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
    public static void sendToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Preconditions.checkState(FMLEnvironment.dist.isClient(), "Cannot send serverbound payloads on the server");
        ClientPacketListener listener = Objects.requireNonNull(Minecraft.getInstance().getConnection());
        listener.send(payload);
        for (CustomPacketPayload otherPayload : payloads) {
            listener.send(otherPayload);
        }
    }

    /**
     * Send the given payload(s) to the given player
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        player.connection.send(makeClientboundPacket(payload, payloads));
    }

    /**
     * Send the given payload(s) to all players in the given dimension
     */
    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        level.getServer().getPlayerList().broadcastAll(makeClientboundPacket(payload, payloads), level.dimension());
    }

    /**
     * Send the given payload(s) to all players in the area covered by the given radius around the given coordinates
     * in the given dimension, except the given excluded player if present
     */
    public static void sendToPlayersNear(
            ServerLevel level,
            @Nullable ServerPlayer excluded,
            double x,
            double y,
            double z,
            double radius,
            CustomPacketPayload payload,
            CustomPacketPayload... payloads) {
        Packet<?> packet = makeClientboundPacket(payload, payloads);
        level.getServer().getPlayerList().broadcast(excluded, x, y, z, radius, level.dimension(), packet);
    }

    /**
     * Send the given payload(s) to all players on the server
     */
    public static void sendToAllPlayers(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "Cannot send clientbound payloads on the client");
        server.getPlayerList().broadcastAll(makeClientboundPacket(payload, payloads));
    }

    /**
     * Send the given payload(s) to all players tracking the given entity
     */
    public static void sendToPlayersTrackingEntity(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.broadcast(entity, makeClientboundPacket(payload, payloads));
        } else {
            throw new IllegalStateException("Cannot send clientbound payloads on the client");
        }
    }

    /**
     * Send the given payload(s) to all players tracking the given entity and the entity itself if it is a player
     */
    public static void sendToPlayersTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.broadcastAndSend(entity, makeClientboundPacket(payload, payloads));
        } else {
            throw new IllegalStateException("Cannot send clientbound payloads on the client");
        }
    }

    /**
     * Send the given payload(s) to all players tracking the chunk at the given position in the given level
     */
    public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunkPos, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        Packet<?> packet = makeClientboundPacket(payload, payloads);
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
            player.connection.send(packet);
        }
    }

    private static Packet<?> makeClientboundPacket(CustomPacketPayload payload, CustomPacketPayload... payloads) {
        if (payloads.length > 0) {
            final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
            packets.add(new ClientboundCustomPayloadPacket(payload));
            for (CustomPacketPayload otherPayload : payloads) {
                packets.add(new ClientboundCustomPayloadPacket(otherPayload));
            }
            return new ClientboundBundlePacket(packets);
        } else {
            return new ClientboundCustomPayloadPacket(payload);
        }
    }
}
