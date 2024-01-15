/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface IBlockGetterExtension {
    /**
     * Get the {@link AuxiliaryLightManager} of the chunk containing the given {@link BlockPos}.
     * <p>
     * The light manager must be used to hold light values controlled by dynamic data from {@link BlockEntity}s
     * to ensure access to the light data is thread-safe and the data is available during chunk load from disk
     * where {@code BlockEntity}s are not yet added to the chunk.
     *
     * @param pos The position for whose containing chunk the light manager is requested
     * @return the light manager or {@code null} if the chunk is not accessible ({@link ChunkSource#getChunkForLighting(int, int)}
     *         returned {@code null}) or the given implementation of {@link BlockGetter} does not implement {@link #getAuxLightManager(ChunkPos)}
     */
    @Nullable
    @ApiStatus.NonExtendable
    default AuxiliaryLightManager getAuxLightManager(BlockPos pos) {
        return getAuxLightManager(new ChunkPos(pos));
    }

    /**
     * Get the {@link AuxiliaryLightManager} of the chunk at the given {@link ChunkPos}.
     * <p>
     * The light manager must be used to hold light values controlled by dynamic data from {@link BlockEntity}s
     * to ensure access to the light data is thread-safe and the data is available during chunk load from disk
     * where {@code BlockEntity}s are not yet added to the chunk.
     *
     * @param pos The position of the chunk from which the light manager is requested
     * @return the light manager or {@code null} if the chunk is not accessible ({@link ChunkSource#getChunkForLighting(int, int)}
     *         returned {@code null}) or the given implementation of {@link BlockGetter} does not implement this method
     */
    @Nullable
    default AuxiliaryLightManager getAuxLightManager(ChunkPos pos) {
        if (this instanceof LevelAccessor level) {
            LightChunk chunk = level.getChunkSource().getChunkForLighting(pos.x, pos.z);
            return chunk != null ? chunk.getAuxLightManager(pos) : null;
        } else if (this instanceof ImposterProtoChunk chunk) {
            return chunk.getWrapped().getAuxLightManager(pos);
        }
        return null;
    }

    /**
     * Retrieves the model data manager for this level.
     * This will be {@code null} on a server level.
     */
    @Nullable
    default ModelDataManager getModelDataManager() {
        return null;
    }
}
