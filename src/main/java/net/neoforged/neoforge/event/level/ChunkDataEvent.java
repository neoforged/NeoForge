/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * ChunkDataEvent is fired when a chunk is about to be loaded from disk or saved to disk.
 */
public abstract class ChunkDataEvent extends ChunkEvent<ChunkAccess> {
    private final SerializableChunkData data;

    public ChunkDataEvent(ChunkAccess chunk, SerializableChunkData data) {
        super(chunk);
        this.data = data;
    }

    public ChunkDataEvent(ChunkAccess chunk, LevelAccessor world, SerializableChunkData data) {
        super(chunk, world);
        this.data = data;
    }

    /**
     * {@return the serialized data of the chunk to be loaded or saved}
     */
    public SerializableChunkData getData() {
        return data;
    }

    /**
     * ChunkDataEvent.Load is fired when the chunk has been created from the provided {@link SerializableChunkData}
     * and is about to be marked as loaded.
     * <p>
     * This event is fired on the main server thread in {@link ChunkMap#scheduleChunkLoad(ChunkPos)}.
     * <p>
     * Saving custom data on a chunk should be handled with data attachments. Interacting with data attachments
     * on the provided chunk is safe in this event.
     * <p>
     * This event is not {@linkplain ICancellableEvent cancellable}.
     * <p>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     **/
    public static class Load extends ChunkDataEvent {
        private final ChunkType status;

        public Load(ChunkAccess chunk, SerializableChunkData data) {
            super(chunk, data);
            this.status = chunk.getPersistedStatus().getChunkType();
        }

        public ChunkType getType() {
            return this.status;
        }
    }

    /**
     * ChunkDataEvent.Save is fired after the chunk has been serialized to the provided {@link SerializableChunkData}
     * which is about to be handed off to a background thread to be written to disk.
     * <p>
     * This event is fired during chunk saving on the main server thread in {@link ChunkMap#save(ChunkAccess)}.
     * <p>
     * Saving custom data on a chunk should be handled with data attachments. Interacting with data attachments
     * on the provided chunk is safe in this event, but any changes done to them in this event will <b>NOT</b>
     * be reflected in the serialized chunk data.
     * <p>
     * This event is not {@linkplain ICancellableEvent cancellable}.
     * <p>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     */
    public static class Save extends ChunkDataEvent {
        public Save(ChunkAccess chunk, LevelAccessor world, SerializableChunkData data) {
            super(chunk, world, data);
        }
    }
}
