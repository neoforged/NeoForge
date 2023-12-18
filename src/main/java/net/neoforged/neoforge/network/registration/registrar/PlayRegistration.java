/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration.registrar;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record PlayRegistration<T extends CustomPacketPayload>(
        FriendlyByteBuf.Reader<T> reader,
        IPlayPayloadHandler<T> handler,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional) implements IPlayPayloadHandler<CustomPacketPayload>, FriendlyByteBuf.Reader<CustomPacketPayload> {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void handle(PlayPayloadContext context, CustomPacketPayload payload) {
        ((IPlayPayloadHandler) handler).handle(context, payload);
    }

    @Override
    public CustomPacketPayload apply(FriendlyByteBuf buffer) {
        return reader.apply(buffer);
    }
}
