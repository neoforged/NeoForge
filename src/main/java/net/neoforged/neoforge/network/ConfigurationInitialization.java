/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.util.function.Consumer;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
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
    /**
     * Method called to add configuration tasks that should run before all others,
     * and most importantly before vanilla's own {@link SynchronizeRegistriesTask}.
     */
    public static void configureEarlyTasks(ServerConfigurationPacketListener listener, Consumer<ConfigurationTask> tasks) {
        if (listener.isConnected(FrozenRegistrySyncStartPayload.TYPE) &&
                listener.isConnected(FrozenRegistryPayload.TYPE) &&
                listener.isConnected(FrozenRegistrySyncCompletedPayload.TYPE)) {
            tasks.accept(new SyncRegistries());
        }
    }

    @SubscribeEvent
    private static void configureModdedClient(OnGameConfigurationEvent event) {
        if (event.getListener().isConnected(ConfigFilePayload.TYPE)) {
            event.register(new SyncConfig(event.getListener()));
        }

        //These two can always be registered they detect the listener connection type internally and will skip themselves.
        event.register(new SyncTierSortingRegistry(event.getListener()));
        event.register(new RegistryDataMapNegotiation(event.getListener()));
    }
}
