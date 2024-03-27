/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A record that holds the information needed to describe a registered play payload, its reader and handler.
 *
 * @param reader   The reader for the payload
 * @param handler  The handler for the payload
 * @param version  The version of the payload
 * @param modId    The id of the mod that registered the payload
 * @param flow     The flow of the payload
 * @param optional Whether the payload is optional
 * @param <T>      The type of the payload
 */
@ApiStatus.Internal
public record PlayRegistration<T extends CustomPacketPayload>(
        FriendlyByteBuf.Reader<T> reader,
        IPlayPayloadHandler<T> handler,
        String modId,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional) implements IPlayPayloadHandler<CustomPacketPayload>, FriendlyByteBuf.Reader<CustomPacketPayload> {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void handle(CustomPacketPayload payload, PlayPayloadContext context) {
        ((IPlayPayloadHandler) handler).handle(payload, context);
    }

    @Override
    public CustomPacketPayload apply(FriendlyByteBuf buffer) {
        return reader.apply(buffer);
    }
}
