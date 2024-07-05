/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a modded network component, indicates what channel and version the client and server
 * agreed upon.
 *
 * @param id      The mod id
 * @param version The mod version, if present
 */
@ApiStatus.Internal
public record ModdedNetworkComponent(ResourceLocation id, Optional<String> version) {
    public static final StreamCodec<FriendlyByteBuf, ModdedNetworkComponent> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            ModdedNetworkComponent::id,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            ModdedNetworkComponent::version,
            ModdedNetworkComponent::new);
}
