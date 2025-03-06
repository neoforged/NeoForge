/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class for events involving chunks.
 * <p>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.
 */
public abstract class ChunkEvent<T extends ChunkAccess> extends LevelEvent {
    private final T chunk;

    public ChunkEvent(T chunk) {
        super(chunk.getLevel());
        this.chunk = chunk;
    }

    public ChunkEvent(T chunk, LevelAccessor level) {
        super(level);
        this.chunk = chunk;
    }

    public T getChunk() {
        return chunk;
    }

    /**
     * This event is fired after Minecraft loads a {@link LevelChunk} into the level, on both the client and server.
     * <p>
     * Specifically, this is fired during chunk loading in {@link ChunkStatusTasks#full}, and when the client receives a chunk from the server.
     * <p>
     * <b>Note:</b> On the server, this event is fired before the underlying {@link LevelChunk} is promoted to {@link ChunkStatus#FULL}.
     * Interactions with the {@link LevelChunk#getLevel() level} must be delayed until the next game tick to prevent deadlocking the game.
     */
    public static class Load extends ChunkEvent<LevelChunk> {
        private final boolean newChunk;

        @ApiStatus.Internal
        public Load(LevelChunk chunk, boolean newChunk) {
            super(chunk);
            this.newChunk = newChunk;
        }

        /**
         * {@return true if this is a newly-generated chunk, instead of one loaded from disk}
         * 
         * @apiNote This method only has meaning on the server, since the client does not generate chunks.
         */
        public boolean isNewChunk() {
            return newChunk;
        }
    }

    /**
     * This event is fired when Minecraft unloads a Chunk from the level, just before the side-specific unload method is called.
     * <p>
     * On the server, this event is fired after the chunk has been saved, and {@link ChunkDataEvent.Save} has been fired.
     */
    public static class Unload extends ChunkEvent<LevelChunk> {
        public Unload(LevelChunk chunk) {
            super(chunk);
        }
    }
}
