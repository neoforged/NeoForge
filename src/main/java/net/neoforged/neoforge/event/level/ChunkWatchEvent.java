/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event is fired whenever a chunk has a watch-related action.
 * <p>
 * The {@linkplain #getPlayer() player}'s level may not be the same as the {@linkplain #getLevel() level of the chunk}
 * when the player is teleporting to another dimension.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable} and does not {@linkplain HasResult have a result}.
 * <p>
 * This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus}
 * only on the {@linkplain LogicalSide#SERVER logical server}.
 **/
public abstract class ChunkWatchEvent extends Event {
    private final ServerLevel level;
    private final ServerPlayer player;
    private final ChunkPos pos;

    public ChunkWatchEvent(ServerPlayer player, ChunkPos pos, ServerLevel level) {
        this.player = player;
        this.pos = pos;
        this.level = level;
    }

    /**
     * {@return the server player involved with the watch action}
     */
    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * {@return the chunk position this watch event is affecting}
     */
    public ChunkPos getPos() {
        return this.pos;
    }

    /**
     * {@return the server level containing the chunk}
     */
    public ServerLevel getLevel() {
        return this.level;
    }

    /**
     * This event is fired whenever a {@link ServerPlayer} begins watching a chunk and the chunk is queued up for
     * sending to the client (see {@link net.minecraft.server.level.ChunkMap#markChunkPendingToSend(ServerPlayer, LevelChunk)}).
     * <p>
     * This event must NOT be used to send additional chunk-related data to the client as the client will not be aware
     * of the chunk yet when this event fires. {@link ChunkWatchEvent.Sent} should be used for this purpose instead
     * <p>
     * This event is not {@linkplain ICancellableEvent cancellable} and does not {@linkplain HasResult have a result}.
     * <p>
     * This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus}
     * only on the {@linkplain LogicalSide#SERVER logical server}.
     **/
    public static class Watch extends ChunkWatchEvent {
        private final LevelChunk chunk;

        public Watch(ServerPlayer player, LevelChunk chunk, ServerLevel level) {
            super(player, chunk.getPos(), level);
            this.chunk = chunk;
        }

        public LevelChunk getChunk() {
            return this.chunk;
        }
    }

    /**
     * This event is fired whenever a chunk being watched by a {@link ServerPlayer} is transmitted to their client
     * (see {@link net.minecraft.server.network.PlayerChunkSender#sendNextChunks(ServerPlayer)}).
     * <p>
     * This event may be used to send additional chunk-related data to the client.
     * <p>
     * This event is not {@linkplain ICancellableEvent cancellable} and does not {@linkplain HasResult have a result}.
     * <p>
     * This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus}
     * only on the {@linkplain LogicalSide#SERVER logical server}.
     **/
    public static class Sent extends ChunkWatchEvent {
        private final LevelChunk chunk;

        public Sent(ServerPlayer player, LevelChunk chunk, ServerLevel level) {
            super(player, chunk.getPos(), level);
            this.chunk = chunk;
        }

        public LevelChunk getChunk() {
            return this.chunk;
        }
    }

    /**
     * This event is fired whenever a {@link ServerPlayer} stops watching a chunk. The chunk this event fires for
     * may never have actually been known to the client if the chunk goes out of range before being sent due to
     * slow pacing of chunk sync on slow connections or to slow clients.
     * <p>
     * This event is fired when a chunk is removed from the watched chunks of an {@link ServerPlayer}
     * in {@link net.minecraft.server.level.ChunkMap#dropChunk(ServerPlayer, ChunkPos)}.
     * <p>
     * This event is not {@linkplain ICancellableEvent cancellable} and does not {@linkplain HasResult have a result}.
     * <p>
     * This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus}
     * only on the {@linkplain LogicalSide#SERVER logical server}.
     **/
    public static class UnWatch extends ChunkWatchEvent {
        public UnWatch(ServerPlayer player, ChunkPos pos, ServerLevel level) {
            super(player, pos, level);
        }
    }
}
