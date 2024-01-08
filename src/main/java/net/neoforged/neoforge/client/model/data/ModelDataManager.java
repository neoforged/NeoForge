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
import net.minecraft.core.SectionPos;
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
 */
@EventBusSubscriber(modid = "neoforge", bus = Bus.FORGE, value = Dist.CLIENT)
public abstract sealed class ModelDataManager permits ModelDataManager.Active, ModelDataManager.Snapshot {
    ModelDataManager() {}

    /**
     * {@return the {@link ModelData} stored for the given position or {@code null} if none is present}
     * 
     * @param pos The position to query
     */
    @Nullable
    public abstract ModelData getAt(BlockPos pos);

    /**
     * {@return the {@link ModelData} stored for the given position or {@link ModelData#EMPTY} if none is present}
     * 
     * @param pos The position to query
     */
    public abstract ModelData getAtOrEmpty(BlockPos pos);

    /**
     * Snapshot the state of this manager for all sections in the volume specified by the given section coordinates.
     * 
     * @throws IllegalArgumentException if this is a snapshot and the given region doesn't match the snapshot's region
     */
    @ApiStatus.Internal
    public abstract ModelDataManager.Snapshot snapshotSectionRegion(int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ);

    /**
     * The active manager owned by the client's level and operated on the main client thread.
     * <p>
     * Users should not be instantiating this themselves unless they know what they're doing.
     */
    @ApiStatus.Internal
    public static final class Active extends ModelDataManager {
        private final Thread owningThread = Thread.currentThread();
        private final Level level;
        private final Long2ObjectMap<Set<BlockPos>> needModelDataRefresh = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Long2ObjectMap<ModelData>> modelDataCache = new Long2ObjectOpenHashMap<>();

        public Active(Level level) {
            this.level = level;
        }

        /**
         * Request a refresh of the stored data for the given {@link BlockEntity}. The given {@code BlockEntity}
         * must be in the level owning this manager
         */
        public void requestRefresh(BlockEntity blockEntity) {
            if (isOtherThread()) {
                throw new UnsupportedOperationException("Cannot request ModelData refresh outside the owning thread: " + owningThread);
            }

            Preconditions.checkNotNull(blockEntity, "BlockEntity must not be null");
            Preconditions.checkState(blockEntity.getLevel() == level, "BlockEntity does not belong to the level owning this manager");
            needModelDataRefresh.computeIfAbsent(SectionPos.asLong(blockEntity.getBlockPos()), $ -> new HashSet<>())
                    .add(blockEntity.getBlockPos());
        }

        @Override
        @Nullable
        public ModelData getAt(BlockPos pos) {
            Preconditions.checkArgument(level.isClientSide, "Cannot request model data for server level");
            long sectionPos = SectionPos.asLong(pos);
            refreshAt(sectionPos);
            return modelDataCache.getOrDefault(sectionPos, Long2ObjectMaps.emptyMap()).get(pos.asLong());
        }

        @Override
        public ModelData getAtOrEmpty(BlockPos pos) {
            return Objects.requireNonNullElse(getAt(pos), ModelData.EMPTY);
        }

        @Override
        public ModelDataManager.Snapshot snapshotSectionRegion(int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ) {
            if (isOtherThread()) {
                throw new UnsupportedOperationException("Cannot snapshot active manager outside the owning thread: " + owningThread);
            }
            return new ModelDataManager.Snapshot(this, sectionMinX, sectionMinY, sectionMinZ, sectionMaxX, sectionMaxY, sectionMaxZ);
        }

        private void refreshAt(long section) {
            if (isOtherThread()) {
                return;
            }

            Set<BlockPos> needUpdate = needModelDataRefresh.remove(section);

            if (needUpdate != null) {
                Long2ObjectMap<ModelData> data = modelDataCache.computeIfAbsent(section, $ -> new Long2ObjectOpenHashMap<>());
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

        private boolean isOtherThread() {
            return Thread.currentThread() != owningThread;
        }
    }

    /**
     * A snapshot of the active manager's state in the specified sections at the point in time when a chunk section was
     * prepared for re-rendering. Holds an immutable copy of the applicable subset of the active manager's state.
     */
    @ApiStatus.Internal
    public static final class Snapshot extends ModelDataManager {
        public static final ModelDataManager.Snapshot EMPTY = new ModelDataManager.Snapshot();

        private final Long2ObjectMap<ModelData> modelDataCache;
        private final long sectionMin;
        private final long sectionMax;

        Snapshot(ModelDataManager.Active srcManager, int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ) {
            this.sectionMin = SectionPos.asLong(sectionMinX, sectionMinY, sectionMinZ);
            this.sectionMax = SectionPos.asLong(sectionMaxX, sectionMaxY, sectionMaxZ);

            Long2ObjectMap<ModelData> cache = new Long2ObjectOpenHashMap<>();
            for (int x = sectionMinX; x <= sectionMaxX; x++) {
                for (int y = sectionMinY; y <= sectionMaxY; y++) {
                    for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                        long sectionPos = SectionPos.asLong(x, y, z);
                        srcManager.refreshAt(sectionPos);
                        cache.putAll(srcManager.modelDataCache.getOrDefault(sectionPos, Long2ObjectMaps.emptyMap()));
                    }
                }
            }
            this.modelDataCache = cache.isEmpty() ? Long2ObjectMaps.emptyMap() : cache;
        }

        private Snapshot() {
            this.sectionMin = this.sectionMax = SectionPos.asLong(0, 0, 0);
            this.modelDataCache = Long2ObjectMaps.emptyMap();
        }

        @Override
        @Nullable
        public ModelData getAt(BlockPos pos) {
            return modelDataCache.get(pos.asLong());
        }

        @Override
        public ModelData getAtOrEmpty(BlockPos pos) {
            return modelDataCache.getOrDefault(pos.asLong(), ModelData.EMPTY);
        }

        @Override
        public ModelDataManager.Snapshot snapshotSectionRegion(int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ) {
            Preconditions.checkArgument(
                    this.sectionMin == SectionPos.asLong(sectionMinX, sectionMinY, sectionMinZ) && this.sectionMax == SectionPos.asLong(sectionMaxX, sectionMaxY, sectionMaxZ),
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
            int maxSection = level.getMaxSection();
            for (int y = level.getMinSection(); y < maxSection; y++) {
                long section = SectionPos.asLong(chunk.x, y, chunk.z);
                activeManager.needModelDataRefresh.remove(section);
                activeManager.modelDataCache.remove(section);
            }
        }
    }
}
