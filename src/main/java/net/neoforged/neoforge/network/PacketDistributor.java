/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * Means to distribute packets in various ways
 */
public class PacketDistributor<T> {
    public static void sendToPlayer(@Nullable ServerPlayer player, CustomPacketPayload payload) {
        if (player != null) {
            player.connection.send(toVanillaPacket(payload));
        }
    }

    /**
     * Send all the players in the dimension.
     */
    public static void sendToDimension(@Nullable ResourceKey<Level> level, CustomPacketPayload payload) {
        if (level != null) {
            getServer().getPlayerList().broadcastAll(toVanillaPacket(payload), level);
        }
    }

    public static void sendNear(@Nullable TargetPoint targetPoint, CustomPacketPayload payload) {
        if (targetPoint != null) {
            getServer().getPlayerList().broadcast(targetPoint.excluded, targetPoint.x, targetPoint.y, targetPoint.z, targetPoint.r2, targetPoint.dim, toVanillaPacket(payload));
        }
    }

    public static void sendToAll(CustomPacketPayload payload) {
        getServer().getPlayerList().broadcastAll(toVanillaPacket(payload));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(toVanillaPacket(payload));
    }

    public static void sendToTrackingEntity(@Nullable Entity entity, CustomPacketPayload payload) {
        if (entity != null) {
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, toVanillaPacket(payload));
        }
    }

    public static void sendToTrackingEntityAndSelf(@Nullable Entity entity, CustomPacketPayload payload) {
        if (entity != null) {
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcastAndSend(entity, toVanillaPacket(payload));
            trackingEntityAndSelf(entity).accept(toVanillaPacket(payload));
        }
    }

    public static void sendToTrackingChunk(@Nullable LevelChunk levelChunk, CustomPacketPayload payload) {
        if (levelChunk != null) {
            ((ServerChunkCache) levelChunk.getLevel().getChunkSource()).chunkMap.getPlayers(levelChunk.getPos(), false).forEach(e -> e.connection.send(toVanillaPacket(payload)));
        }
    }

    private static Packet<?> toVanillaPacket(CustomPacketPayload payload) {
        return new ClientboundCustomPayloadPacket(payload);
    }

    public static final class TargetPoint {
        private final ServerPlayer excluded;
        private final double x;
        private final double y;
        private final double z;
        private final double r2;
        private final ResourceKey<Level> dim;

        /**
         * A target point with excluded entity
         *
         * @param excluded Entity to exclude
         * @param x        X
         * @param y        Y
         * @param z        Z
         * @param r2       Radius
         * @param dim      DimensionType
         */
        public TargetPoint(final ServerPlayer excluded, final double x, final double y, final double z, final double r2, final ResourceKey<Level> dim) {
            this.excluded = excluded;
            this.x = x;
            this.y = y;
            this.z = z;
            this.r2 = r2;
            this.dim = dim;
        }

        /**
         * A target point without excluded entity
         *
         * @param x   X
         * @param y   Y
         * @param z   Z
         * @param r2  Radius
         * @param dim DimensionType
         */
        public TargetPoint(final double x, final double y, final double z, final double r2, final ResourceKey<Level> dim) {
            this.excluded = null;
            this.x = x;
            this.y = y;
            this.z = z;
            this.r2 = r2;
            this.dim = dim;
        }

        /**
         * Helper to build a TargetPoint without excluded Entity
         *
         * @param x   X
         * @param y   Y
         * @param z   Z
         * @param r2  Radius
         * @param dim DimensionType
         * @return A TargetPoint supplier
         */
        public static Supplier<TargetPoint> p(double x, double y, double z, double r2, ResourceKey<Level> dim) {
            TargetPoint tp = new TargetPoint(x, y, z, r2, dim);
            return () -> tp;
        }
    }

    /**
     * A Distributor curried with a specific value instance, for actual dispatch
     */
    public static class PacketTarget {
        private final Consumer<Packet<?>> packetConsumer;
        private final PacketDistributor<?> distributor;

        PacketTarget(final Consumer<Packet<?>> packetConsumer, final PacketDistributor<?> distributor) {
            this.packetConsumer = packetConsumer;
            this.distributor = distributor;
        }

        public void send(Packet<?> packet) {
            packetConsumer.accept(packet);
        }

        public void send(CustomPacketPayload... payloads) {
            if (flow().isClientbound()) {
                if (payloads.length > 1) {
                    final List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();
                    for (CustomPacketPayload payload : payloads) {
                        packets.add(new ClientboundCustomPayloadPacket(payload));
                    }
                    this.send(new ClientboundBundlePacket(packets));
                } else if (payloads.length == 1) {
                    this.send(new ClientboundCustomPayloadPacket(payloads[0]));
                }
            } else {
                for (CustomPacketPayload payload : payloads) {
                    this.send(new ServerboundCustomPayloadPacket(payload));
                }
            }
        }

        public PacketFlow flow() {
            return distributor.flow;
        }
    }

    private final BiFunction<PacketDistributor<T>, T, Consumer<Packet<?>>> functor;
    private final PacketFlow flow;

    public PacketDistributor(BiFunction<PacketDistributor<T>, T, Consumer<Packet<?>>> functor, PacketFlow flow) {
        this.functor = functor;
        this.flow = flow;
    }

    public PacketDistributor(Function<PacketDistributor<T>, Consumer<Packet<?>>> functor, PacketFlow flow) {
        this((d, t) -> functor.apply(d), flow);
    }

    private static Consumer<Packet<?>> playerConsumer(final ServerPlayer player) {
        return p -> player.connection.send(p);
    }

    private static Consumer<Packet<?>> playerListDimConsumer(final ResourceKey<Level> dimensionType) {
        return p -> getServer().getPlayerList().broadcastAll(p, dimensionType);
    }

    private static Consumer<Packet<?>> playerListAll() {
        return p -> getServer().getPlayerList().broadcastAll(p);
    }

    private static Consumer<Packet<?>> clientToServer() {
        return p -> Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(p);
    }

    private static Consumer<Packet<?>> playerListPointConsumer(final TargetPoint targetPoint) {
        return p -> {
            getServer().getPlayerList().broadcast(targetPoint.excluded, targetPoint.x, targetPoint.y, targetPoint.z, targetPoint.r2, targetPoint.dim, p);
        };
    }

    private static Consumer<Packet<?>> trackingEntity(final Entity entity) {
        return p -> ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, p);
    }

    private static Consumer<Packet<?>> trackingEntityAndSelf(final Entity entity) {
        return p -> ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcastAndSend(entity, p);
    }

    private static Consumer<Packet<?>> trackingChunk(final LevelChunk chunkPos) {
        return p -> ((ServerChunkCache) chunkPos.getLevel().getChunkSource()).chunkMap.getPlayers(chunkPos.getPos(), false).forEach(e -> e.connection.send(p));
    }

    private static MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
