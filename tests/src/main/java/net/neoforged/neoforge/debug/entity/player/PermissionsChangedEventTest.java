/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PermissionsChangedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("permissions_changed_event_test")
@Mod.EventBusSubscriber
public class PermissionsChangedEventTest {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onPermissionChanged(PermissionsChangedEvent event) {
        LOGGER.info("{} permission level changed to {} from {}",
                event.getEntity().getName().getString(),
                event.getNewLevel(),
                event.getOldLevel());
    }
}
