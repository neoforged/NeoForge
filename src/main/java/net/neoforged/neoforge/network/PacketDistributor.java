/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Means to distribute packets in various ways
 */
public class PacketDistributor<T> {
    /**
     * Send to the player specified in the Supplier
     * <br/>
     * {@link #with(Supplier)} Player
     */
    public static final PacketDistributor<ServerPlayer> PLAYER = new PacketDistributor<>(PacketDistributor::playerConsumer, PacketFlow.CLIENTBOUND);
    /**
     * Send to everyone in the dimension specified in the Supplier
     * <br/>
     * {@link #with(Supplier)} DimensionType
     */
    public static final PacketDistributor<ResourceKey<Level>> DIMENSION = new PacketDistributor<>(PacketDistributor::playerListDimConsumer, PacketFlow.CLIENTBOUND);
    /**
     * Send to everyone near the {@link TargetPoint} specified in the Supplier
     * <br/>
     * {@link #with(Supplier)} TargetPoint
     */
    public static final PacketDistributor<TargetPoint> NEAR = new PacketDistributor<>(PacketDistributor::playerListPointConsumer, PacketFlow.CLIENTBOUND);
    /**
     * Send to everyone
     * <br/>
     * {@link #noArg()}
     */
    public static final PacketDistributor<Void> ALL = new PacketDistributor<>(PacketDistributor::playerListAll, PacketFlow.CLIENTBOUND);
    /**
     * Send to the server (CLIENT to SERVER)
     * <br/>
     * {@link #noArg()}
     */
    public static final PacketDistributor<Void> SERVER = new PacketDistributor<>(PacketDistributor::clientToServer, PacketFlow.SERVERBOUND);
    /**
     * Send to all tracking the Entity in the Supplier
     * <br/>
     * {@link #with(Supplier)} Entity
     */
    public static final PacketDistributor<Entity> TRACKING_ENTITY = new PacketDistributor<>(PacketDistributor::trackingEntity, PacketFlow.CLIENTBOUND);
    /**
     * Send to all tracking the Entity and Player in the Supplier
     * <br/>
     * {@link #with(Supplier)} Entity
     */
    public static final PacketDistributor<Entity> TRACKING_ENTITY_AND_SELF = new PacketDistributor<>(PacketDistributor::trackingEntityAndSelf, PacketFlow.CLIENTBOUND);
    /**
     * Send to all tracking the Chunk in the Supplier
     * <br/>
     * {@link #with(Supplier)} Chunk
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
        private final Consumer<CustomPacketPayload> packetConsumer;
        private final PacketDistributor<?> distributor;

        PacketTarget(final Consumer<CustomPacketPayload> packetConsumer, final PacketDistributor<?> distributor) {
            this.packetConsumer = packetConsumer;
            this.distributor = distributor;
        }

        public void send(CustomPacketPayload packet) {
            packetConsumer.accept(packet);
        }

        public PacketFlow getFlow() {
            return distributor.flow;
        }

    }

    private final BiFunction<PacketDistributor<T>, Supplier<T>, Consumer<CustomPacketPayload>> functor;
    private final PacketFlow flow;

    public PacketDistributor(BiFunction<PacketDistributor<T>, Supplier<T>, Consumer<CustomPacketPayload>> functor, PacketFlow flow) {
        this.functor = functor;
        this.flow = flow;
    }

    /**
     * Apply the supplied value to the specific distributor to generate an instance for sending packets to.
     * 
     * @param input The input to apply
     * @return A curried instance
     */
    public PacketTarget with(Supplier<T> input) {
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
        return new PacketTarget(functor.apply(this, () -> null), this);
    }

    private Consumer<CustomPacketPayload> playerConsumer(final Supplier<ServerPlayer> entityPlayerMPSupplier) {
        return p -> entityPlayerMPSupplier.get().connection.send(p);
    }

    private Consumer<CustomPacketPayload> playerListDimConsumer(final Supplier<ResourceKey<Level>> dimensionTypeSupplier) {
        return p -> getServer().getPlayerList().broadcastAll(p, dimensionTypeSupplier.get());
    }

    private Consumer<CustomPacketPayload> playerListAll(final Supplier<Void> voidSupplier) {
        return p -> getServer().getPlayerList().broadcastAll(p);
    }

    private Consumer<CustomPacketPayload> clientToServer(final Supplier<Void> voidSupplier) {
        return p -> Objects.requireNonNull(Minecraft.getInstance().getConnection()).send(p);
    }

    private Consumer<CustomPacketPayload> playerListPointConsumer(final Supplier<TargetPoint> targetPointSupplier) {
        return p -> {
            final TargetPoint tp = targetPointSupplier.get();
            getServer().getPlayerList().broadcast(tp.excluded, tp.x, tp.y, tp.z, tp.r2, tp.dim, p);
        };
    }

    private Consumer<CustomPacketPayload> trackingEntity(final Supplier<Entity> entitySupplier) {
        return p -> {
            final Entity entity = entitySupplier.get();
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, p);
        };
    }

    private Consumer<CustomPacketPayload> trackingEntityAndSelf(final Supplier<Entity> entitySupplier) {
        return p -> {
            final Entity entity = entitySupplier.get();
            ((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcastAndSend(entity, p);
        };
    }

    private Consumer<CustomPacketPayload> trackingChunk(final Supplier<LevelChunk> chunkPosSupplier) {
        return p -> {
            final LevelChunk chunk = chunkPosSupplier.get();
            ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false).forEach(e -> e.connection.send(p));
        };
    }

    private MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
