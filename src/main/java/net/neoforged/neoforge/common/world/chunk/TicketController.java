/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import java.util.UUID;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ForcedChunksSavedData;

/**
 * A class used to manage chunk loading tickets associated with a specific ID.
 * <p>
 * Controllers must be registered via {@link RegisterTicketControllersEvent}. A controller that isn't registered will have all of its tickets discarded, and the chunks will no longer be forcefully loaded.
 *
 * @param id       the ID of this controller
 * @param callback a callback to be called when the tickets are loaded, in order to validate whether they're still active or not. {@link LoadingValidationCallback#NOOP} should be used when a callback needn't be provided
 */
@ParametersAreNonnullByDefault
public record TicketController(ResourceLocation id, LoadingValidationCallback callback) {
    /**
     * Creates a ticket controller without a validation callback.
     *
     * @param id the ID of the controller
     */
    public TicketController(ResourceLocation id) {
        this(id, LoadingValidationCallback.NOOP);
    }

    /**
     * Forces a chunk to be loaded with the "owner" of the ticket being a given block position.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     */
    public boolean forceChunk(ServerLevel level, BlockPos owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return ForcedChunkManager.forceChunk(level, id, owner, chunkX, chunkZ, add, ticking, ticking ? ForcedChunkManager.BLOCK_TICKING : ForcedChunkManager.BLOCK, ForcedChunksSavedData::getBlockForcedChunks);
    }

    /**
     * Forces a chunk to be loaded with the "owner" of the ticket being the UUID of the given entity.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     */
    public boolean forceChunk(ServerLevel level, Entity owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return forceChunk(level, owner.getUUID(), chunkX, chunkZ, add, ticking);
    }

    /**
     * Forces a chunk to be loaded with the "owner" of the ticket being a given UUID.
     *
     * @param add     {@code true} to force the chunk, {@code false} to unforce the chunk.
     * @param ticking {@code true} to make the chunk receive full chunk ticks even if there is no player nearby.
     */
    public boolean forceChunk(ServerLevel level, UUID owner, int chunkX, int chunkZ, boolean add, boolean ticking) {
        return ForcedChunkManager.forceChunk(level, id(), owner, chunkX, chunkZ, add, ticking, ticking ? ForcedChunkManager.ENTITY_TICKING : ForcedChunkManager.ENTITY, ForcedChunksSavedData::getEntityForcedChunks);
    }
}
