/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * A record that holds the information needed to describe a registered play payload, its reader and handler.
 *
 * @param reader   The reader for the payload
 * @param handler  The handler for the payload
 * @param version  The version of the payload
 * @param flow     The flow of the payload
 * @param optional Whether the payload is optional
 * @param <T>      The type of the payload
 */
@ApiStatus.Internal
public record PlayRegistration<T extends CustomPacketPayload>(
        FriendlyByteBuf.Reader<T> reader,
        IPayloadHandler<T> handler,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional) implements IPayloadHandler<T>, FriendlyByteBuf.Reader<T> {
    @Override
    public T apply(FriendlyByteBuf buffer) {
        return reader.apply(buffer);
    }

    @Override
    public void handle(T payload, IPayloadContext context) {
        this.handler.handle(payload, context);
    }
}
