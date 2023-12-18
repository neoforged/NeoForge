/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration.registrar;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;

import java.util.Optional;
import java.util.OptionalInt;

public record ConfigurationRegistration<T extends CustomPacketPayload>(
        FriendlyByteBuf.Reader<T> reader,
        IConfigurationPayloadHandler<T> handler,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional
) implements IConfigurationPayloadHandler<CustomPacketPayload>, FriendlyByteBuf.Reader<CustomPacketPayload> {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handle(ConfigurationPayloadContext context, CustomPacketPayload payload) {
        ((IConfigurationPayloadHandler) handler).handle(context, payload);
    }
    
    @Override
    public CustomPacketPayload apply(FriendlyByteBuf buffer) {
        return reader.apply(buffer);
    }
}
