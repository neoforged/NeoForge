/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a potential modded network component, used for querying the client for modded network components.
 *
 * @param id       The id of the component
 * @param version  The version of the component, if present
 * @param flow     The flow of the component, if present
 * @param optional Whether the component is optional
 */
@ApiStatus.Internal
public record ModdedNetworkQueryComponent(ResourceLocation id, Optional<String> version, Optional<PacketFlow> flow, boolean optional) {
    public static final StreamCodec<FriendlyByteBuf, ModdedNetworkQueryComponent> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            ModdedNetworkQueryComponent::id,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            ModdedNetworkQueryComponent::version,
            ByteBufCodecs.optional(NeoForgeStreamCodecs.enumCodec(PacketFlow.class)),
            ModdedNetworkQueryComponent::flow,
            ByteBufCodecs.BOOL,
            ModdedNetworkQueryComponent::optional,
            ModdedNetworkQueryComponent::new);
}
