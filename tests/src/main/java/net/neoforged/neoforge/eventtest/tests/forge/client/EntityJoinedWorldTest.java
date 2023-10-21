/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.eventtest.tests.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.eventtest.internal.EventTest;

/**
 * Test case for the EntityJoinWorldEvent.
 *
 * Event fires during gameplay.
 * Success condition:
 *      EntityJoinWorldEvent fired for Minecraft.getInstance().player
 * Failure info:
 *      "EntityJoinWorldEvent was not fired"
 *
 */

// @TestHolder("EntityJoinWorld")
public class EntityJoinedWorldTest extends EventTest {

    @Override
    public void registerEvents() {
        MinecraftForge.EVENT_BUS.addListener(this::eventListener);
    }

    private void eventListener(EntityJoinLevelEvent event) {
        final Entity eventEntity = event.getEntity();
        final Player gamePlayer = Minecraft.getInstance().player;

        final boolean eventFiredForPlayer = eventEntity == gamePlayer;

        if(eventFiredForPlayer)
            pass();
    }
}