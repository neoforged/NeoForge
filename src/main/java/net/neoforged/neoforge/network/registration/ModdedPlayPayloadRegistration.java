/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;

/**
 * Registration for a custom packet payload.
 * This type holds the negotiated preferredVersion of the payload to use, and the handler for it.
 *
 * @param <T> The type of payload.
 */
public record ModdedPlayPayloadRegistration<T extends CustomPacketPayload>(
        ResourceLocation id,
        Class<T> type,
        IPlayPayloadHandler<T> handler,
        FriendlyByteBuf.Reader<T> reader
) {
}
