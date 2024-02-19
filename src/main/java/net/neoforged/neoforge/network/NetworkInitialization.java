/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.network.payload.TierSortingRegistryPayload;
import net.neoforged.neoforge.network.payload.TierSortingRegistrySyncCompletePayload;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.ClientRegistryManager;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ApiStatus.Internal
public class NetworkInitialization {
    @SubscribeEvent
    private static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(NeoForgeVersion.MOD_ID)
                .versioned(NeoForgeVersion.getSpec())
                .optional();
        registrar
                .common(
                        TierSortingRegistryPayload.ID,
                        TierSortingRegistryPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .common(
                        ConfigFilePayload.ID,
                        ConfigFilePayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .configuration(
                        FrozenRegistrySyncStartPayload.ID,
                        FrozenRegistrySyncStartPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .configuration(
                        FrozenRegistryPayload.ID,
                        FrozenRegistryPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .configuration(
                        FrozenRegistrySyncCompletedPayload.ID,
                        FrozenRegistrySyncCompletedPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle)
                                .server(ServerPayloadHandler.getInstance()::handle))
                .configuration(
                        TierSortingRegistrySyncCompletePayload.ID,
                        TierSortingRegistrySyncCompletePayload::new,
                        handlers -> handlers.server(ServerPayloadHandler.getInstance()::handle))
                .configuration(
                        KnownRegistryDataMapsPayload.ID,
                        KnownRegistryDataMapsPayload::new,
                        handlers -> handlers.client(ClientRegistryManager::handleKnownDataMaps))
                .configuration(
                        KnownRegistryDataMapsReplyPayload.ID,
                        KnownRegistryDataMapsReplyPayload::new,
                        handlers -> handlers.server(RegistryManager::handleKnownDataMapsReply))
                .play(
                        AdvancedAddEntityPayload.ID,
                        AdvancedAddEntityPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .play(
                        AdvancedOpenScreenPayload.ID,
                        AdvancedOpenScreenPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .play(
                        AuxiliaryLightDataPayload.ID,
                        AuxiliaryLightDataPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle))
                .play(
                        RegistryDataMapSyncPayload.ID,
                        RegistryDataMapSyncPayload::decode,
                        handlers -> handlers.client(ClientRegistryManager::handleDataMapSync))
                .play(AdvancedContainerSetDataPayload.ID,
                        AdvancedContainerSetDataPayload::new,
                        handlers -> handlers.client(ClientPayloadHandler.getInstance()::handle));
    }
}
