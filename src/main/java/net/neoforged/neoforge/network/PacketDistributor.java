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

/**
 * Means to distribute packets in various ways
 */
public class PacketDistributor<T> {
    /**
     * Sends a client-bound payload to the specified player.
     * <br/>
     * {@link #with(Object)} Player
     */
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(PacketDistributor::playerConsumer, PacketFlow.CLIENTBOUND);

    /**
     * Sends a client-bound payload to everyone in the specified dimension.
     * <br/>
     * {@link #with(Object)} DimensionType
     */
    public static final PacketDistributor<ResourceKey<Level>> DIMENSION = new PacketDistributor<>(PacketDistributor::playerListDimConsumer, PacketFlow.CLIENTBOUND);

    /**
     * Sends a client-bound payload to everyone near the specified {@link TargetPoint}.
     * <br/>
     * {@link #with(Object)} TargetPoint
     */
    public static final PacketDistributor<TargetPoint> NEAR = new PacketDistributor<>(PacketDistributor::playerListPointConsumer, PacketFlow.CLIENTBOUND);

    /**
     * Sends a client-bound payload to all players connected to the server.
     */
    public static final NoArgDistributor ALL = new NoArgDistributor(PacketDistributor::playerListAll, PacketFlow.CLIENTBOUND);

    /**
     * Sends a server-bound payload to the server.
     */
    public static final NoArgDistributor SERVER = new NoArgDistributor(PacketDistributor::clientToServer, PacketFlow.SERVERBOUND);

    /**
     * Sends a client-bound payload to all players tracking the entity.
     * <br/>
     * {@link #with(Object)} Entity
     */
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(PacketDistributor::trackingEntity, PacketFlow.CLIENTBOUND);

    /**
     * Sends a client-bound payload to all players tracking the entity, and the entity if it is a player.
     * <br/>
     * {@link #with(Object)} Entity
     */
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(PacketDistributor::trackingEntityAndSelf, PacketFlow.CLIENTBOUND);

    /**
     * Sends a client-bound payload to all players tracking the chunk
     * <br/>
     * {@link #with(Object)} Chunk
     */
    public static final PacketDistributor<LevelChunk> TRACKING_CHUNK = new PacketDistributor<>(PacketDistributor::trackingChunk, PacketFlow.CLIENTBOUND);

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
     *
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

    /**
     * Apply the supplied value to the specific distributor to generate an instance for sending packets to.
     * 
     * @param input The input to apply
     * @return A curried instance
     */
    public PacketTarget with(T input) {
        return new PacketTarget(functor.apply(this, input), this);
    }

    /**
     * Apply a no argument value to a distributor to generate an instance for sending packets to.
     *
     * @see #ALL
     * @see #SERVER
     * @return A curried instance
     */
    public PacketTarget noArg() {
        return new PacketTarget(functor.apply(this, null), this);
    }

    private Consumer<Packet<?>> playerConsumer(final ServerPlayer entityPlayerMP) {
        return p -> entityPlayerMP.connection.send(p);
    }

    private Consumer<Packet<?>> playerListDimConsumer(final ResourceKey<Level> dimensionType) {
        return p -> getServer().getPlayerList().broadcastAll(p, dimensionType);
    }

    private Consumer<Packet<?>> playerListAll() {
        return p -> getServer().getPlayerList().broadcastAll(p);
    }

    private Consumer<Packet<?>> clientToServer() {
        return p -> Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(p);
    }

    private Consumer<Packet<?>> playerListPointConsumer(final TargetPoint targetPoint) {
        return p -> {
            final TargetPoint tp = targetPoint;
            getServer().getPlayerList().broadcast(tp.excluded, tp.x, tp.y, tp.z, tp.r2, tp.dim, p);
        };
    }

    private Consumer<Packet<?>> trackingEntity(final Entity entity) {
        return p -> {
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, p);
        };
    }

    private Consumer<Packet<?>> trackingEntityAndSelf(final Entity entity) {
        return p -> {
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcastAndSend(entity, p);
        };
    }

    @SuppressWarnings("resource")
    private Consumer<Packet<?>> trackingChunk(final LevelChunk chunkPos) {
        return p -> {
            ((ServerChunkCache) chunkPos.getLevel().getChunkSource()).chunkMap.getPlayers(chunkPos.getPos(), false).forEach(e -> e.connection.send(p));
        };
    }

    private MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static class NoArgDistributor extends PacketDistributor<Void> {
        public NoArgDistributor(Function<PacketDistributor<Void>, Consumer<Packet<?>>> functor, PacketFlow flow) {
            super(functor, flow);
        }

        public void send(Packet<?> packet) {
            noArg().send(packet);
        }

        public void send(CustomPacketPayload... payloads) {
            noArg().send(payloads);
        }
    }
}
