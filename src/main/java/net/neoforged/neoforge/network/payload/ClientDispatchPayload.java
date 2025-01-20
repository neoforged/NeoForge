package net.neoforged.neoforge.network.payload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Internal marker classes for packets for which the handler dispatch happens in {@code ClientPayloadHandler}.
 * This is meant to be a temporary workaround until we rework the networking API to allow for separate handler registration.
 */
public sealed interface ClientDispatchPayload extends CustomPacketPayload
        permits ConfigFilePayload,
        FrozenRegistrySyncStartPayload,
        FrozenRegistryPayload,
        FrozenRegistrySyncCompletedPayload,
        KnownRegistryDataMapsPayload,
        AdvancedAddEntityPayload,
        AdvancedOpenScreenPayload,
        AuxiliaryLightDataPayload,
        RegistryDataMapSyncPayload,
        AdvancedContainerSetDataPayload,
        ClientboundCustomSetTimePayload {}
