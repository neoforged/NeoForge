/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.client.registries.ClientRegistryManager;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.RecipeContentPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
public final class ClientNetworkInitialization {
    private ClientNetworkInitialization() {}

    @SubscribeEvent
    private static void register(RegisterClientPayloadHandlersEvent event) {
        event.register(ConfigFilePayload.TYPE, ClientPayloadHandler::handle);
        event.register(FrozenRegistrySyncStartPayload.TYPE, ClientPayloadHandler::handle);
        event.register(FrozenRegistryPayload.TYPE, ClientPayloadHandler::handle);
        event.register(FrozenRegistrySyncCompletedPayload.TYPE, ClientPayloadHandler::handle);
        event.register(KnownRegistryDataMapsPayload.TYPE, ClientRegistryManager::handleKnownDataMaps);
        event.register(AdvancedAddEntityPayload.TYPE, ClientPayloadHandler::handle);
        event.register(AdvancedOpenScreenPayload.TYPE, ClientPayloadHandler::handle);
        event.register(AuxiliaryLightDataPayload.TYPE, ClientPayloadHandler::handle);
        event.register(RegistryDataMapSyncPayload.TYPE, ClientRegistryManager::handleDataMapSync);
        event.register(AdvancedContainerSetDataPayload.TYPE, ClientPayloadHandler::handle);
        event.register(ClientboundCustomSetTimePayload.TYPE, ClientPayloadHandler::handle);
        event.register(RecipeContentPayload.TYPE, ClientPayloadHandler::handle);
    }
}
