/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holds the information needed to describe a registered payload.
 * 
 * @param type      The type of the payload
 * @param codec     The codec for the payload
 * @param handler   The handler for the payload
 * @param protocols The protocols this payload supports
 * @param flow      The flow this payload supports (empty if both)
 * @param version   The version of the payload
 * @param optional  If the payload is optional
 * @param <T>       The type of the payload
 */
@ApiStatus.Internal
public record PayloadRegistration<T extends CustomPacketPayload>(
        CustomPacketPayload.Type<T> type,
        StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
        IPayloadHandler<T> handler,
        List<ConnectionProtocol> protocols,
        Optional<PacketFlow> flow,
        String version,
        boolean optional) {
    public ResourceLocation id() {
        return this.type().id();
    }

    /**
     * {@return true if the registered flow is compatible with the passed flow}
     */
    public boolean matchesFlow(PacketFlow flow) {
        return this.flow.isEmpty() || this.flow.get() == flow;
    }
}
