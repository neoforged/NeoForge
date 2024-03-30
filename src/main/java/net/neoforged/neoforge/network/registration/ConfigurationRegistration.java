/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * A record that holds the information needed to describe a registered configuration payload, its reader and handler.
 *
 * @param reader   The reader for the payload
 * @param handler  The handler for the payload
 * @param id       The identifier of the payload
 * @param version  The version of the payload
 * @param flow     The flow of the payload
 * @param optional Whether the payload is optional
 * @param <T>      The type of the payload
 */
@ApiStatus.Internal
public record ConfigurationRegistration<T extends CustomPacketPayload>(
        FriendlyByteBuf.Reader<T> reader,
        IConfigurationPayloadHandler<T> handler,
        ResourceLocation id,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional) implements IConfigurationPayloadHandler<CustomPacketPayload>, FriendlyByteBuf.Reader<CustomPacketPayload> {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void handle(CustomPacketPayload payload, ConfigurationPayloadContext context) {
        ((IConfigurationPayloadHandler) handler).handle(payload, context);
    }

    @Override
    public CustomPacketPayload apply(FriendlyByteBuf buffer) {
        return reader.apply(buffer);
    }
}
