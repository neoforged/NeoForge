/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = "neoforge", bus = EventBusSubscriber.Bus.GAME)
public class PointOfInterestUnloading {
    /**
     * The goal here is to fix the POI memory leak that happens due to
     * {@link net.minecraft.world.level.chunk.storage.SectionStorage#storage} field never
     * actually removing POIs long after they become irrelevant. We do it here in chunk unload event
     * so that chunk that are fully unloaded now gets the POI removed from the POI cached storage map.
     */
    @SubscribeEvent
    public static void OnChunkUnload(ChunkEvent.Unload event) {
        ChunkSource chunkSource = event.getLevel().getChunkSource();
        if (chunkSource instanceof ServerChunkCache serverChunkCache) {
            ChunkAccess chunkAccess = event.getChunk();
            ChunkPos chunkPos = chunkAccess.getPos();
            PoiManager poiManager = serverChunkCache.chunkMap.poiManager();
            poiManager.flush(chunkPos); // Make sure all POI in chunk are saved to disk first.
            poiManager.remove(chunkPos.toLong()); // Remove the cached POIs for this chunk's location.
        }
    }
}
