/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.misc;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("send_datapacks_to_client")
@Mod.EventBusSubscriber
public class OnDatapackSynctEventTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    protected static void onSendDataToClient(final OnDatapackSyncEvent event) {
        // Fire for a specific player on login
        if (event.getPlayer() != null)
            syncData(event.getPlayer());
        // Fire for all players on /reload
        else
            event.getPlayerList().getPlayers().forEach(OnDatapackSynctEventTest::syncData);
    }

    private static void syncData(ServerPlayer player) {
        LOGGER.info("Sending modded datapack data to {}", player.getName().getString());
    }
}
