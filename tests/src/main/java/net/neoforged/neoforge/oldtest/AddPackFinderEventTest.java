/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.BuiltInPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
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
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            var resourcePath = ModList.get().getModFileById(MODID).getFile().findResource("test_nested_resource_pack");
            var pack = Pack.readMetaAndCreate(new PackLocationInfo("builtin/add_pack_finders_test", Component.literal("display name"), PackSource.BUILT_IN, Optional.empty()), BuiltInPackSource.fromName((path) -> new PathPackResources(path, resourcePath)), PackType.CLIENT_RESOURCES, new PackSelectionConfig(true, Pack.Position.BOTTOM, false));
            event.addRepositorySource((packConsumer) -> packConsumer.accept(pack));
        }
    }
}
