/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

public class PointOfInterestUnloading {
    /**
     * The goal here is to fix the POI memory leak that happens due to
     * {@link net.minecraft.world.level.chunk.storage.SectionStorage#storage} field never
     * actually removing POIs long after they become irrelevant. We do it here in chunk unload event
     * so that chunk that are fully unloaded now gets the POI removed from the POI cached storage map.
     */
    public static void OnChunkUnload(PoiManager poiManager, ChunkAccess chunkAccess) {
        ChunkPos chunkPos = chunkAccess.getPos();
        poiManager.flush(chunkPos); // Make sure all POI in chunk are saved to disk first.

        // Remove the cached POIs for this chunk's location.
        int SectionPosMinY = SectionPos.blockToSectionCoord(chunkAccess.getMinBuildHeight());
        for (int currentSectionY = 0; currentSectionY < chunkAccess.getSectionsCount(); currentSectionY++) {
            long sectionPosKey = SectionPos.asLong(chunkPos.x, SectionPosMinY + currentSectionY, chunkPos.z);
            poiManager.remove(sectionPosKey);
        }
    }
}
