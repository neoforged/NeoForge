/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.configuration.CheckExtensibleEnums;
import net.neoforged.neoforge.network.configuration.CheckFeatureFlags;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.payload.AdvancedAddEntityPayload;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import net.neoforged.neoforge.network.payload.AuxiliaryLightDataPayload;
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
import net.neoforged.neoforge.network.payload.SyncAttachmentsPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@EventBusSubscriber(modid = NeoForgeVersion.MOD_ID)
final class NetworkInitialization {
    private NetworkInitialization() {}

    @SubscribeEvent
    private static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1") // Update this version if the payload semantics change.
                .optional();
        registrar
                .commonToClient(
                        ConfigFilePayload.TYPE,
                        ConfigFilePayload.STREAM_CODEC)
                .configurationToClient(
                        FrozenRegistrySyncStartPayload.TYPE,
                        FrozenRegistrySyncStartPayload.STREAM_CODEC)
                .configurationToClient(
                        FrozenRegistryPayload.TYPE,
                        FrozenRegistryPayload.STREAM_CODEC)
                .configurationBidirectional(
                        FrozenRegistrySyncCompletedPayload.TYPE,
                        FrozenRegistrySyncCompletedPayload.STREAM_CODEC,
                        ServerPayloadHandler::handle)
                .configurationToClient(
                        KnownRegistryDataMapsPayload.TYPE,
                        KnownRegistryDataMapsPayload.STREAM_CODEC)
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
                        AdvancedAddEntityPayload.STREAM_CODEC)
                .playToClient(
                        AdvancedOpenScreenPayload.TYPE,
                        AdvancedOpenScreenPayload.STREAM_CODEC)
                .playToClient(
                        AuxiliaryLightDataPayload.TYPE,
                        AuxiliaryLightDataPayload.STREAM_CODEC)
                .playToClient(
                        RegistryDataMapSyncPayload.TYPE,
                        RegistryDataMapSyncPayload.STREAM_CODEC)
                .playToClient(AdvancedContainerSetDataPayload.TYPE,
                        AdvancedContainerSetDataPayload.STREAM_CODEC)
                .playToClient(
                        ClientboundCustomSetTimePayload.TYPE,
                        ClientboundCustomSetTimePayload.STREAM_CODEC)
                .playToClient(
                        RecipeContentPayload.TYPE,
                        RecipeContentPayload.STREAM_CODEC)
                .playToClient(
                        SyncAttachmentsPayload.TYPE,
                        SyncAttachmentsPayload.STREAM_CODEC);
    }
}
