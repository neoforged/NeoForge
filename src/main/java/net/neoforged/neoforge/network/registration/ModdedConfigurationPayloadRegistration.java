/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import org.jetbrains.annotations.ApiStatus;

/**
 * Registration for a custom packet payload.
 * This type holds the negotiated preferredVersion of the payload to use, and the handler for it.
 *
 * @param id      The id of the payload.
 * @param type    The type of payload.
 * @param handler The handler for the payload.
 * @param reader  The reader for the payload.
 * @param <T>     The type of payload.
 */
@ApiStatus.Internal
public record ModdedConfigurationPayloadRegistration<T extends CustomPacketPayload>(
        ResourceLocation id,
        Class<T> type,
        IConfigurationPayloadHandler<T> handler,
        FriendlyByteBuf.Reader<T> reader) {}
