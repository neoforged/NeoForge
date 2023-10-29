/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("player_xp_event_test")
@Mod.EventBusSubscriber()
public class PlayerXpEventTest {

    private static final boolean ENABLE = false;
    private static Logger logger = LogManager.getLogger(PlayerXpEventTest.class);

    @SubscribeEvent
    public static void onPlayerPickupXp(PlayerXpEvent.PickupXp event) {
        if (!ENABLE) return;
        logger.info("{} picked up an experience orb worth {}", event.getEntity().getName().getString(), event.getOrb().getValue());
    }

    @SubscribeEvent
    public static void onPlayerXpChange(PlayerXpEvent.XpChange event) {
        if (!ENABLE) return;
        logger.info("{} has been given {} experience", event.getEntity().getName().getString(), event.getAmount());
    }

    @SubscribeEvent
    public static void onPlayerLevelChange(PlayerXpEvent.LevelChange event) {
        if (!ENABLE) return;
        logger.info("{} has changed {} levels", event.getEntity().getName().getString(), event.getLevels());
    }

}
