/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.world;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Mod(ChunkWatchEventTest.MODID)
public class ChunkWatchEventTest {
    public static final String MODID = "chunkwatchworldtest";

    private static final boolean ENABLED = false;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object2IntMap<UUID> WATCHED_BY_PLAYER = new Object2IntOpenHashMap<>();

    public ChunkWatchEventTest() {
        if (ENABLED) {
            NeoForge.EVENT_BUS.register(ChunkWatchEventTest.class);
        }
    }

    @SubscribeEvent
    public static void onWatch(ChunkWatchEvent.Watch event) {
        int watched = WATCHED_BY_PLAYER.getInt(event.getPlayer().getUUID());
        ++watched;
        WATCHED_BY_PLAYER.put(event.getPlayer().getUUID(), watched);
        LOGGER.info("Watching chunk {} in dimension {}. Player's dimension: {}, total chunks watched by player {}",
                event.getPos(), getDimensionName(event.getLevel()), getDimensionName(event.getPlayer().getCommandSenderWorld()),
                watched);
    }

    @SubscribeEvent
    public static void onSent(ChunkWatchEvent.Sent event) {
        LOGGER.info("Watched chunk {} in dimension {} sent to client. Player's dimension: {}",
                event.getPos(), getDimensionName(event.getLevel()), getDimensionName(event.getPlayer().getCommandSenderWorld()));
    }

    @SubscribeEvent
    public static void onUnwatch(ChunkWatchEvent.UnWatch event) {
        int watched = WATCHED_BY_PLAYER.getInt(event.getPlayer().getUUID());
        --watched;
        WATCHED_BY_PLAYER.put(event.getPlayer().getUUID(), watched);
        LOGGER.info("Unwatching chunk {} in dimension {}. Player's dimension: {}, total chunks watched by player {}",
                event.getPos(), getDimensionName(event.getLevel()), getDimensionName(event.getPlayer().getCommandSenderWorld()),
                watched);
    }

    @Nullable
    private static ResourceLocation getDimensionName(Level w) {
        return w.dimension().location();
    }
}
