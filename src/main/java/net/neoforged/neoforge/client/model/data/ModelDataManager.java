/*
 * Copyright (c) NeoForge and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A manager for the lifecycle of all the {@link ModelData} instances in a {@link Level}.
 * <p>
 * Users should not be instantiating or using this themselves unless they know what they're doing.
 */
@ApiStatus.Internal
@EventBusSubscriber(modid = "neoforge", bus = Bus.FORGE, value = Dist.CLIENT)
public abstract sealed class ModelDataManager permits ModelDataManager.Active, ModelDataManager.Snapshot {
    ModelDataManager() {}

    public abstract void requestRefresh(BlockEntity blockEntity);

    @Nullable
    public abstract ModelData getAt(BlockPos pos);

    public abstract ModelData getAtOrEmpty(BlockPos pos);

    public abstract ModelDataManager.Snapshot snapshotChunkRegion(int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ);

    public static final class Active extends ModelDataManager {
        private final Level level;
        private final Long2ObjectMap<Set<BlockPos>> needModelDataRefresh = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Long2ObjectMap<ModelData>> modelDataCache = new Long2ObjectOpenHashMap<>();

        public Active(Level level) {
            this.level = level;
        }

        @Override
        public void requestRefresh(BlockEntity blockEntity) {
            Preconditions.checkNotNull(blockEntity, "Block entity must not be null");
            needModelDataRefresh.computeIfAbsent(ChunkPos.asLong(blockEntity.getBlockPos()), $ -> new HashSet<>())
                    .add(blockEntity.getBlockPos());
        }

        @Override
        public ModelData getAt(BlockPos pos) {
            Preconditions.checkArgument(level.isClientSide, "Cannot request model data for server level");
            long chunkPos = ChunkPos.asLong(pos);
            refreshAt(chunkPos);
            return modelDataCache.getOrDefault(chunkPos, Long2ObjectMaps.emptyMap()).get(pos.asLong());
        }

        @Override
        public ModelData getAtOrEmpty(BlockPos pos) {
            return Objects.requireNonNullElse(getAt(pos), ModelData.EMPTY);
        }

        @Override
        public ModelDataManager.Snapshot snapshotChunkRegion(int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ) {
            return new ModelDataManager.Snapshot(this, chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ);
        }

        private void refreshAt(long chunk) {
            Set<BlockPos> needUpdate = needModelDataRefresh.remove(chunk);

            if (needUpdate != null) {
                Long2ObjectMap<ModelData> data = modelDataCache.computeIfAbsent(chunk, $ -> new Long2ObjectOpenHashMap<>());
                for (BlockPos pos : needUpdate) {
                    BlockEntity toUpdate = level.getBlockEntity(pos);
                    if (toUpdate != null && !toUpdate.isRemoved()) {
                        data.put(pos.asLong(), toUpdate.getModelData());
                    } else {
                        data.remove(pos.asLong());
                    }
                }
            }
        }
    }

    public static final class Snapshot extends ModelDataManager {
        public static final ModelDataManager.Snapshot EMPTY = new ModelDataManager.Snapshot();

        private final Long2ObjectMap<ModelData> modelDataCache = new Long2ObjectOpenHashMap<>();
        private final int chunkMinX;
        private final int chunkMinZ;
        private final int chunkMaxX;
        private final int chunkMaxZ;

        Snapshot(ModelDataManager.Active srcManager, int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ) {
            this.chunkMinX = chunkMinX;
            this.chunkMinZ = chunkMinZ;
            this.chunkMaxX = chunkMaxX;
            this.chunkMaxZ = chunkMaxZ;

            for (int x = chunkMinX; x <= chunkMaxX; x++) {
                for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                    long chunkPos = ChunkPos.asLong(x, z);
                    srcManager.refreshAt(chunkPos);
                    modelDataCache.putAll(srcManager.modelDataCache.getOrDefault(chunkPos, Long2ObjectMaps.emptyMap()));
                }
            }
        }

        private Snapshot() {
            this.chunkMinX = this.chunkMinZ = this.chunkMaxX = this.chunkMaxZ = 0;
        }

        @Override
        public void requestRefresh(BlockEntity blockEntity) {
            throw new UnsupportedOperationException("Cannot request data refresh on snapshot");
        }

        @Override
        public ModelData getAt(BlockPos pos) {
            return modelDataCache.get(pos.asLong());
        }

        @Override
        public ModelData getAtOrEmpty(BlockPos pos) {
            return modelDataCache.getOrDefault(pos.asLong(), ModelData.EMPTY);
        }

        @Override
        public ModelDataManager.Snapshot snapshotChunkRegion(int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ) {
            Preconditions.checkArgument(
                    this.chunkMinX == chunkMinX && this.chunkMinZ == chunkMinZ && this.chunkMaxX == chunkMaxX && this.chunkMaxZ == chunkMaxZ,
                    "Cannot request snapshot for a different range from this snapshot");
            return this;
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        var level = event.getChunk().getWorldForge();
        if (level == null)
            return;

        var modelDataManager = level.getModelDataManager();
        if (modelDataManager instanceof Active activeManager) {
            ChunkPos chunk = event.getChunk().getPos();
            activeManager.needModelDataRefresh.remove(chunk.toLong());
            activeManager.modelDataCache.remove(chunk.toLong());
        }
    }
}
