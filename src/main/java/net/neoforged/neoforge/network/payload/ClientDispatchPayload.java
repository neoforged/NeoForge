/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Internal marker classes for packets for which the handler dispatch happens in {@code ClientPayloadHandler}.
 * This is meant to be a temporary workaround until we rework the networking API to allow for separate handler registration.
 */
public sealed interface ClientDispatchPayload extends CustomPacketPayload
        permits AdvancedAddEntityPayload,
        AdvancedContainerSetDataPayload,
        AdvancedOpenScreenPayload,
        AuxiliaryLightDataPayload,
        ClientboundCustomSetTimePayload,
        ConfigFilePayload,
        FrozenRegistryPayload,
        FrozenRegistrySyncCompletedPayload,
        FrozenRegistrySyncStartPayload,
        KnownRegistryDataMapsPayload,
        RecipeContentPayload,
        RegistryDataMapSyncPayload {}
