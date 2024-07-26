/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity.player;

import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ClientInformationUpdatedEvent;

/**
 * Tests the client info update event by telling a player their new and old info on updates.
 * Easiest thing to test is change your language or view distance.
 */
@Mod(ClientInformationUpdatedTest.MOD_ID)
public class ClientInformationUpdatedTest {
    public static final String MOD_ID = "client_information_updated_test";

    public ClientInformationUpdatedTest() {
        NeoForge.EVENT_BUS.addListener((ClientInformationUpdatedEvent event) -> {
            event.getEntity().sendSystemMessage(Component.literal("old: " + event.getOldInformation()));
            event.getEntity().sendSystemMessage(Component.literal("new: " + event.getUpdatedInformation()));
        });
    }
}
