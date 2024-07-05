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
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.configuration.RegistryDataMapNegotiation;
import net.neoforged.neoforge.network.configuration.SyncConfig;
import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = "neoforge", bus = EventBusSubscriber.Bus.MOD)
public class ConfigurationInitialization {
    /**
     * Method called to add configuration tasks that should run before all others,
     * and most importantly before vanilla's own {@link SynchronizeRegistriesTask}.
     */
    public static void configureEarlyTasks(ServerConfigurationPacketListener listener, Consumer<ConfigurationTask> tasks) {
        if (listener.hasChannel(FrozenRegistrySyncStartPayload.TYPE) &&
                listener.hasChannel(FrozenRegistryPayload.TYPE) &&
                listener.hasChannel(FrozenRegistrySyncCompletedPayload.TYPE)) {
            tasks.accept(new SyncRegistries());
        }
    }

    @SubscribeEvent
    private static void configureModdedClient(RegisterConfigurationTasksEvent event) {
        if (event.getListener().hasChannel(ConfigFilePayload.TYPE)) {
            event.register(new SyncConfig(event.getListener()));
        }

        //These two can always be registered they detect the listener connection type internally and will skip themselves.
        event.register(new RegistryDataMapNegotiation(event.getListener()));
    }
}
