/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.world;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChunkDataEventSaveNullWorldTest.MODID)
@Mod.EventBusSubscriber
public class ChunkDataEventSaveNullWorldTest {
    static final String MODID = "chunk_data_event_save_null_world_test";
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    @SubscribeEvent
    public static void onChunkSave(final ChunkDataEvent.Save event) {
        if (event.getLevel() == null) {
            LOGGER.info("Chunk at {} had null world", event.getChunk().getPos());
        }
    }
}
