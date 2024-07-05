/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a complete negotiated network payload type, which is stored on the client and server.
 *
 * @param id            The payload id.
 * @param chosenVersion The chosen version, if any.
 */
@ApiStatus.Internal
public record NetworkChannel(ResourceLocation id, String chosenVersion) {
    public static final StreamCodec<FriendlyByteBuf, NetworkChannel> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, NetworkChannel::id,
            ByteBufCodecs.STRING_UTF8, NetworkChannel::chosenVersion,
            NetworkChannel::new);
}
