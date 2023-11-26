/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("entity_teleport_event_test")
public class EntityTeleportEventTest {
    public static final boolean ENABLE = true;
    public static final Logger LOGGER = LogManager.getLogger();

    public EntityTeleportEventTest() {
        if (ENABLE)
            NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void entityTeleport(EntityTeleportEvent event) {
        LOGGER.info("{} teleporting from {} to {}", event.getEntity(), event.getPrev(), event.getTarget());
    }
}
