/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.entity.player;

import net.minecraftforge.event.entity.player.PlayerSpawnPhantomsEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * This tests for {@link PlayerSpawnPhantomsEventTest} and fires when the normal Phantom Spawns could occur.
 * In Game use, `/time set night`, then wait until natural spawns occur.
 * This test modifies the number of spawns to 20 as well as forcing it to happen regardless of the players last sleep time.
 * Vanilla has a minimum of 3 days since last sleep before a maximum of 4 (on hard mode) phantoms can spawn
 */
@Mod("player_spawn_phantoms_event_test")
@Mod.EventBusSubscriber()
public class PlayerSpawnPhantomsEventTest
{

    private static final boolean ENABLE = false;

    @SubscribeEvent
    public static void onPhantomsSpawn(PlayerSpawnPhantomsEvent event)
    {
        if (!ENABLE) return;
        event.setPhantomsToSpawn(20);
        event.setResult(Event.Result.ALLOW);
    }

}
