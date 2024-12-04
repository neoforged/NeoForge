/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
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
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MODID, "test_disabled_data_pack"),
                PackType.SERVER_DATA,
                Component.literal("Disabled-By-Default DataPack Name"),
                PackSource.FEATURE,
                false,
                Pack.Position.TOP);

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MODID, "test_enabled_data_pack"),
                PackType.SERVER_DATA,
                Component.literal("Enabled-By-Default DataPack Name"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP);

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MODID, "test_disabled_resource_pack"),
                PackType.CLIENT_RESOURCES,
                Component.literal("Disabled-By-Default ResourcePack Name"),
                PackSource.BUILT_IN,
                false,
                Pack.Position.TOP);

        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(MODID, "test_always_enabled_resource_pack"),
                PackType.CLIENT_RESOURCES,
                Component.literal("Forced-Enabled-Always ResourcePack Name"),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP);
    }
}
