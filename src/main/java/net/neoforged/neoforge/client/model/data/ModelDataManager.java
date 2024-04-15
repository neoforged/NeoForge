/*
 * Copyright (c) NeoForge and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;

/**
 * A manager for the lifecycle of all the {@link ModelData} instances in a {@link Level}.
 *
 * Users should not instantiate this unless they know what they are doing. The manager is also not thread-safe,
 * it should only be interacted with on the main client thread.
 */
@EventBusSubscriber(modid = "neoforge", bus = Bus.FORGE, value = Dist.CLIENT)
public class ModelDataManager {
    private final Thread owningThread = Thread.currentThread();
    private final Level level;
    private final Long2ObjectMap<Set<BlockPos>> needModelDataRefresh = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Long2ObjectMap<ModelData>> modelDataCache = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectFunction<ModelData> EMPTY_SNAPSHOT = pos -> ModelData.EMPTY;

    public ModelDataManager(Level level) {
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

    /**
     * Provides all the model data for a given chunk section. This is useful for mods which wish to retrieve
     * a fast view of the model data for a single section in a level.
     * <p></p>
     * The returned map must be copied if it needs to be accessed from another thread, as it may be modified
     * by this data manager.
     *
     * @param pos the section to query
     * @return an (unmodifiable) map containing the {@link ModelData} stored for the given chunk section
     */
    @UnmodifiableView
    public Long2ObjectMap<ModelData> getAt(SectionPos pos) {
        long sectionKey = pos.asLong();
        refreshAt(sectionKey);
        var map = modelDataCache.get(sectionKey);
        if (map != null) {
            return Long2ObjectMaps.unmodifiable(map);
        } else {
            return Long2ObjectMaps.emptyMap();
        }
    }

    /**
     * Retrieves model data for a block at the given position.
     *
     * @param pos the position to query
     * @return the model data at this position, or {@link ModelData#EMPTY} if none exists
     */
    public ModelData getAt(BlockPos pos) {
        Preconditions.checkArgument(level.isClientSide, "Cannot request model data for server level");
        long sectionPos = SectionPos.asLong(pos);
        refreshAt(sectionPos);
        return modelDataCache.getOrDefault(sectionPos, Long2ObjectMaps.emptyMap()).getOrDefault(pos.asLong(), ModelData.EMPTY);
    }

    /**
     * Snapshot the state of this manager for all sections in the volume specified by the given section coordinates.
     * The snapshot will return {@link ModelData#EMPTY} for nonexistent keys.
     */
    @Unmodifiable
    public Long2ObjectFunction<ModelData> snapshotSectionRegion(int sectionMinX, int sectionMinY, int sectionMinZ, int sectionMaxX, int sectionMaxY, int sectionMaxZ) {
        if (isOtherThread()) {
            throw new UnsupportedOperationException("Cannot snapshot active manager outside the owning thread: " + owningThread);
        }
        Long2ObjectMap<ModelData> cache = new Long2ObjectOpenHashMap<>();
        cache.defaultReturnValue(ModelData.EMPTY);
        for (int x = sectionMinX; x <= sectionMaxX; x++) {
            for (int y = sectionMinY; y <= sectionMaxY; y++) {
                for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                    long sectionPos = SectionPos.asLong(x, y, z);
                    refreshAt(sectionPos);
                    cache.putAll(modelDataCache.getOrDefault(sectionPos, Long2ObjectMaps.emptyMap()));
                }
            }
        }
        return cache.isEmpty() ? EMPTY_SNAPSHOT : cache;
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
                ModelData newData = ModelData.EMPTY;
                // Query the BE for new model data if it exists
                if (toUpdate != null && !toUpdate.isRemoved()) {
                    newData = toUpdate.getModelData();
                    // Sanity check so that mods cannot cause impossible-to-trace NPEs in other code later
                    //noinspection ConstantValue
                    if (newData == null) {
                        throw new NullPointerException("Null ModelData provided by " + BlockEntityType.getKey(toUpdate.getType()) + " @ " + pos);
                    }
                }
                // Make sure we don't bother storing empty data in the map
                if (newData != ModelData.EMPTY) {
                    data.put(pos.asLong(), newData);
                } else {
                    data.remove(pos.asLong());
                }
            }
            // Remove the map completely if it's now empty
            if (data.isEmpty()) {
                modelDataCache.remove(section);
            }
        }
    }

    private boolean isOtherThread() {
        return Thread.currentThread() != owningThread;
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        var level = event.getChunk().getLevel();
        if (level == null)
            return;

        var modelDataManager = level.getModelDataManager();
        if (modelDataManager != null) {
            ChunkPos chunk = event.getChunk().getPos();
            int maxSection = level.getMaxSection();
            for (int y = level.getMinSection(); y < maxSection; y++) {
                long section = SectionPos.asLong(chunk.x, y, chunk.z);
                modelDataManager.needModelDataRefresh.remove(section);
                modelDataManager.modelDataCache.remove(section);
            }
        }
    }
}
