/*
 * Copyright (c) NeoForge and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.model.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jetbrains.annotations.UnmodifiableView;

/// A manager for the lifecycle of all the [ModelData] instances in a [Level].
///
/// Users should not instantiate this unless they know what they are doing. The manager is also not thread-safe,
/// it should only be interacted with on the main client thread.
@EventBusSubscriber(modid = "neoforge", value = Dist.CLIENT)
public class ModelDataManager {
    private final Thread owningThread = Thread.currentThread();
    private final Level level;
    private final Long2ObjectMap<LongSet> needModelDataRefresh = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Long2ObjectMap<ModelData>> modelDataCache = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectFunction<ModelData> EMPTY_SNAPSHOT = _ -> ModelData.EMPTY;

    public ModelDataManager(Level level) {
        this.level = level;
    }

    /// Request a refresh of the stored data for the given [BlockPos].
    ///
    /// The given `Pos` must be in the level owning this manager
    ///
    /// @param pos The pos to refresh the stored data for.
    public void requestRefresh(BlockPos pos) {
        if (isOtherThread()) {
            throw new UnsupportedOperationException("Cannot request ModelData refresh outside the owning thread: " + owningThread);
        }

        Preconditions.checkNotNull(pos, "Pos must not be null");
        needModelDataRefresh.computeIfAbsent(SectionPos.asLong(pos), _ -> new LongOpenHashSet()).add(pos.asLong());
    }

    /// Request a refresh of the stored data for the given [BlockEntity].
    ///
    /// The given `BlockEntity` must be in the level owning this manager
    ///
    /// @param blockEntity The block entity to refresh the stored data for
    public void requestRefresh(BlockEntity blockEntity) {
        if (isOtherThread()) {
            throw new UnsupportedOperationException("Cannot request ModelData refresh outside the owning thread: " + owningThread);
        }

        Preconditions.checkNotNull(blockEntity, "BlockEntity must not be null");
        Preconditions.checkState(blockEntity.getLevel() == level, "BlockEntity does not belong to the level owning this manager");
        requestRefresh(blockEntity.getBlockPos());
    }

    /// Provides all the model data for a given chunk section. This is useful for mods which wish to retrieve
    /// a fast view of the model data for a single section in a level.
    ///
    /// The returned map must be copied if it needs to be accessed from another thread, as it may be modified
    /// by this data manager.
    ///
    /// @param pos the section to query
    /// @return an (unmodifiable) map containing the [ModelData] stored for the given chunk section
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

    /// Retrieves model data for a block at the given position.
    ///
    /// @param pos the position to query
    /// @return the model data at this position, or [ModelData#EMPTY] if none exists
    public ModelData getAt(BlockPos pos) {
        Preconditions.checkArgument(level.isClientSide(), "Cannot request model data for server level");
        long sectionPos = SectionPos.asLong(pos);
        refreshAt(sectionPos);
        return modelDataCache.getOrDefault(sectionPos, Long2ObjectMaps.emptyMap()).getOrDefault(pos.asLong(), ModelData.EMPTY);
    }

    /// Snapshot the state of this manager for all sections in the volume specified by the given section coordinates.
    /// The snapshot will return [ModelData#EMPTY] for nonexistent keys.
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

        LongSet needUpdate = needModelDataRefresh.remove(section);

        if (needUpdate != null) {
            Long2ObjectMap<ModelData> data = modelDataCache.computeIfAbsent(section, _ -> new Long2ObjectOpenHashMap<>());
            LongIterator itr = needUpdate.longIterator();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            while (itr.hasNext()) {
                long key = itr.nextLong();
                pos.set(key);
                BlockState toUpdate = level.getBlockState(pos);
                ModelData newData = toUpdate.getModelData(level, pos);

                // Sanity check so that mods cannot cause impossible-to-trace NPEs in other code later
                //noinspection ConstantValue
                if (newData == null) {
                    throw new NullPointerException("Null ModelData provided by " + toUpdate.typeHolder().getRegisteredName() + " @ " + pos);
                }

                // Make sure we don't bother storing empty data in the map
                if (newData != ModelData.EMPTY) {
                    data.put(key, newData);
                } else {
                    data.remove(key);
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
            int maxSection = level.getMaxSectionY();
            for (int y = level.getMinSectionY(); y < maxSection; y++) {
                long section = SectionPos.asLong(chunk.x(), y, chunk.z());
                modelDataManager.needModelDataRefresh.remove(section);
                modelDataManager.modelDataCache.remove(section);
            }
        }
    }
}
