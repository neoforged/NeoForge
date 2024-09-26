/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.flag.ClientboundSyncFlags;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.configuration.CheckExtensibleEnums;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.ExtensibleEnumAcknowledgePayload;
import net.neoforged.neoforge.network.payload.ExtensibleEnumDataPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.ClientRegistryManager;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkInitialization {
    @SubscribeEvent
    private static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1") // Update this version if the payload semantics change.
                .optional();
        registrar
                .configurationToClient(
                        ConfigFilePayload.TYPE,
                        ConfigFilePayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .configurationToClient(
                        FrozenRegistrySyncStartPayload.TYPE,
                        FrozenRegistrySyncStartPayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .configurationToClient(
                        FrozenRegistryPayload.TYPE,
                        FrozenRegistryPayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .configurationBidirectional(
                        FrozenRegistrySyncCompletedPayload.TYPE,
                        FrozenRegistrySyncCompletedPayload.STREAM_CODEC,
                        new DirectionalPayloadHandler<>(ClientPayloadHandler::handle, ServerPayloadHandler::handle))
                .configurationToClient(
                        KnownRegistryDataMapsPayload.TYPE,
                        KnownRegistryDataMapsPayload.STREAM_CODEC,
                        ClientRegistryManager::handleKnownDataMaps)
                .configurationToClient(
                        ExtensibleEnumDataPayload.TYPE,
                        ExtensibleEnumDataPayload.STREAM_CODEC,
                        CheckExtensibleEnums::handleClientboundPayload)
                .configurationToServer(
                        KnownRegistryDataMapsReplyPayload.TYPE,
                        KnownRegistryDataMapsReplyPayload.STREAM_CODEC,
                        RegistryManager::handleKnownDataMapsReply)
                .configurationToServer(
                        ExtensibleEnumAcknowledgePayload.TYPE,
                        ExtensibleEnumAcknowledgePayload.STREAM_CODEC,
                        CheckExtensibleEnums::handleServerboundPayload)
                .playToClient(
                        AdvancedAddEntityPayload.TYPE,
                        AdvancedAddEntityPayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .playToClient(
                        AdvancedOpenScreenPayload.TYPE,
                        AdvancedOpenScreenPayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .playToClient(
                        AuxiliaryLightDataPayload.TYPE,
                        AuxiliaryLightDataPayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .playToClient(
                        RegistryDataMapSyncPayload.TYPE,
                        RegistryDataMapSyncPayload.STREAM_CODEC,
                        ClientRegistryManager::handleDataMapSync)
                .playToClient(AdvancedContainerSetDataPayload.TYPE,
                        AdvancedContainerSetDataPayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .playToClient(
                        ClientboundCustomSetTimePayload.TYPE,
                        ClientboundCustomSetTimePayload.STREAM_CODEC,
                        ClientPayloadHandler::handle)
                .playToClient(
                        ClientboundSyncFlags.TYPE,
                        ClientboundSyncFlags.STREAM_CODEC,
                        ClientboundSyncFlags::handle);
    }
}
