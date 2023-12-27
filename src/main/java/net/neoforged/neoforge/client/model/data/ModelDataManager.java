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
import net.minecraft.client.Minecraft;
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

    public abstract ModelDataManager.Snapshot snapshotSectionRegion(int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ);

    public static final class Active extends ModelDataManager {
        private final Level level;
        private final Long2ObjectMap<Set<BlockPos>> needModelDataRefresh = new Long2ObjectOpenHashMap<>();
        private final Long2ObjectMap<Long2ObjectMap<ModelData>> modelDataCache = new Long2ObjectOpenHashMap<>();

        public Active(Level level) {
            this.level = level;
        }

        @Override
        public void requestRefresh(BlockEntity blockEntity) {
            if (!Minecraft.getInstance().isSameThread()) {
                throw new UnsupportedOperationException("Cannot refresh BlockEntity outside the client thread");
            }

            Preconditions.checkNotNull(blockEntity, "Block entity must not be null");
            needModelDataRefresh.computeIfAbsent(SectionPos.asLong(blockEntity.getBlockPos()), $ -> new HashSet<>())
                    .add(blockEntity.getBlockPos());
        }

        @Override
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
            return new ModelDataManager.Snapshot(this, sectionMinX, sectionMinY, sectionMinZ, sectionMaxX, sectionMaxY, sectionMaxZ);
        }

        private void refreshAt(long section) {
            if (!Minecraft.getInstance().isSameThread()) {
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
    }

    public static final class Snapshot extends ModelDataManager {
        public static final ModelDataManager.Snapshot EMPTY = new ModelDataManager.Snapshot();

        private final Long2ObjectMap<ModelData> modelDataCache = new Long2ObjectOpenHashMap<>();
        private final long sectionMin;
        private final long sectionMax;

        Snapshot(ModelDataManager.Active srcManager, int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ) {
            this.sectionMin = sectionMinX;
            this.sectionMax = sectionMaxZ;

            for (int x = sectionMinX; x <= sectionMaxX; x++) {
                for (int y = sectionMinY; y < sectionMaxY; y++) {
                    for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                        long sectionPos = SectionPos.asLong(x, y, z);
                        srcManager.refreshAt(sectionPos);
                        modelDataCache.putAll(srcManager.modelDataCache.getOrDefault(sectionPos, Long2ObjectMaps.emptyMap()));
                    }
                }
            }
        }

        private Snapshot() {
            this.sectionMin = this.sectionMax = SectionPos.asLong(0, 0, 0);
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
