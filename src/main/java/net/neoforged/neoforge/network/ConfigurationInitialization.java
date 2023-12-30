/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.configuration.SyncConfig;
import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.configuration.SyncTierSortingRegistry;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(modid = "neoforge", bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class ConfigurationInitialization {

    @SubscribeEvent
    private static void configureModdedClient(OnGameConfigurationEvent event) {
        if (event.getListener().isVanillaConnection())
            return;

        event.register(new SyncRegistries());
        event.register(new SyncConfig(event.getListener()));
        event.register(new SyncTierSortingRegistry(event.getListener()));
    }
}
