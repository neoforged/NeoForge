/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.configuration.RegistryDataMapNegotiation;
import net.neoforged.neoforge.network.configuration.SyncConfig;
import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.configuration.SyncTierSortingRegistry;
import net.neoforged.neoforge.network.event.OnGameConfigurationEvent;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(modid = "neoforge", bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class ConfigurationInitialization {
    @SubscribeEvent
    private static void configureModdedClient(OnGameConfigurationEvent event) {
        if (event.getListener().isConnected(FrozenRegistrySyncStartPayload.ID) &&
                event.getListener().isConnected(FrozenRegistryPayload.ID) &&
                event.getListener().isConnected(FrozenRegistrySyncCompletedPayload.ID)) {
            event.register(new SyncRegistries());
        }

        if (event.getListener().isConnected(ConfigFilePayload.ID)) {
            event.register(new SyncConfig(event.getListener()));
        }

        //These two can always be registered they detect the listener connection type internally and will skip themselves.
        event.register(new SyncTierSortingRegistry(event.getListener()));
        event.register(new RegistryDataMapNegotiation(event.getListener()));
    }
}
