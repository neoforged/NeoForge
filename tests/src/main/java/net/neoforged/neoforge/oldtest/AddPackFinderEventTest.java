/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(AddPackFinderEventTest.MODID)
public class AddPackFinderEventTest {
    private static final boolean ENABLE = false;
    public static final String MODID = "add_pack_finders_test";

    public AddPackFinderEventTest(IEventBus modEventBus) {
        if (!ENABLE)
            return;

        modEventBus.register(this);
    }

    @SubscribeEvent
    public void addPackFinders(AddPackFindersEvent event) {
        event.addPackFinders(new ResourceLocation(MODID, "test_nested_resource_pack"), PackType.CLIENT_RESOURCES, Component.literal("display name"), true);
    }
}
