/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.internal.NeoForgeProxy;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.configuration.CheckExtensibleEnums;
import net.neoforged.neoforge.network.configuration.CheckFeatureFlags;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
import net.neoforged.neoforge.network.payload.ClientDispatchPayload;
import net.neoforged.neoforge.network.payload.ClientboundCustomSetTimePayload;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import net.neoforged.neoforge.network.payload.ExtensibleEnumAcknowledgePayload;
import net.neoforged.neoforge.network.payload.ExtensibleEnumDataPayload;
import net.neoforged.neoforge.network.payload.FeatureFlagAcknowledgePayload;
import net.neoforged.neoforge.network.payload.FeatureFlagDataPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RecipeContentPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkInitialization {
    private static <T extends ClientDispatchPayload> IPayloadHandler<T> clientHandler() {
        return NeoForgeProxy.INSTANCE::handleClientPayload;
    }

    @SubscribeEvent
    private static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1") // Update this version if the payload semantics change.
                .optional();
        registrar
                .commonToClient(
                        ConfigFilePayload.TYPE,
                        ConfigFilePayload.STREAM_CODEC,
                        clientHandler())
                .configurationToClient(
                        FrozenRegistrySyncStartPayload.TYPE,
                        FrozenRegistrySyncStartPayload.STREAM_CODEC,
                        clientHandler())
                .configurationToClient(
                        FrozenRegistryPayload.TYPE,
                        FrozenRegistryPayload.STREAM_CODEC,
                        clientHandler())
                .configurationBidirectional(
                        FrozenRegistrySyncCompletedPayload.TYPE,
                        FrozenRegistrySyncCompletedPayload.STREAM_CODEC,
                        new DirectionalPayloadHandler<>(clientHandler(), ServerPayloadHandler::handle))
                .configurationToClient(
                        KnownRegistryDataMapsPayload.TYPE,
                        KnownRegistryDataMapsPayload.STREAM_CODEC,
                        clientHandler())
                .configurationToClient(
                        ExtensibleEnumDataPayload.TYPE,
                        ExtensibleEnumDataPayload.STREAM_CODEC,
                        CheckExtensibleEnums::handleClientboundPayload)
                .configurationToClient(
                        FeatureFlagDataPayload.TYPE,
                        FeatureFlagDataPayload.STREAM_CODEC,
                        CheckFeatureFlags::handleClientboundPayload)
                .configurationToServer(
                        KnownRegistryDataMapsReplyPayload.TYPE,
                        KnownRegistryDataMapsReplyPayload.STREAM_CODEC,
                        RegistryManager::handleKnownDataMapsReply)
                .configurationToServer(
                        ExtensibleEnumAcknowledgePayload.TYPE,
                        ExtensibleEnumAcknowledgePayload.STREAM_CODEC,
                        CheckExtensibleEnums::handleServerboundPayload)
                .configurationToServer(
                        FeatureFlagAcknowledgePayload.TYPE,
                        FeatureFlagAcknowledgePayload.STREAM_CODEC,
                        CheckFeatureFlags::handleServerboundPayload)
                .playToClient(
                        AdvancedAddEntityPayload.TYPE,
                        AdvancedAddEntityPayload.STREAM_CODEC,
                        clientHandler())
                .playToClient(
                        AdvancedOpenScreenPayload.TYPE,
                        AdvancedOpenScreenPayload.STREAM_CODEC,
                        clientHandler())
                .playToClient(
                        AuxiliaryLightDataPayload.TYPE,
                        AuxiliaryLightDataPayload.STREAM_CODEC,
                        clientHandler())
                .playToClient(
                        RegistryDataMapSyncPayload.TYPE,
                        RegistryDataMapSyncPayload.STREAM_CODEC,
                        clientHandler())
                .playToClient(AdvancedContainerSetDataPayload.TYPE,
                        AdvancedContainerSetDataPayload.STREAM_CODEC,
                        clientHandler())
                .playToClient(
                        ClientboundCustomSetTimePayload.TYPE,
                        ClientboundCustomSetTimePayload.STREAM_CODEC,
                        clientHandler())
                .playToClient(
                        RecipeContentPayload.TYPE,
                        RecipeContentPayload.STREAM_CODEC,
                        clientHandler());
    }
}
