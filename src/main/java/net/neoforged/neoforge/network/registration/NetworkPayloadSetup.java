/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.negotiation.NegotiationResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a complete negotiated network, which is stored on the client and server.
 *
 * @param configuration The configuration channels.
 * @param play          The play channels.
 */
@ApiStatus.Internal
public record NetworkPayloadSetup(Map<ConnectionProtocol, Map<ResourceLocation, NetworkChannel>> channels) {
    public static StreamCodec<FriendlyByteBuf, NetworkPayloadSetup> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(IdentityHashMap::new,
                    ByteBufCodecs.idMapper(b -> ConnectionProtocol.values()[b], ConnectionProtocol::ordinal),
                    ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, NetworkChannel.STREAM_CODEC)),
            NetworkPayloadSetup::channels, NetworkPayloadSetup::new);

    public Map<ResourceLocation, NetworkChannel> getChannels(ConnectionProtocol protocol) {
        return this.channels().getOrDefault(protocol, Collections.emptyMap());
    }

    @Nullable
    public NetworkChannel getChannel(ConnectionProtocol protocol, ResourceLocation id) {
        return getChannels(protocol).get(id);
    }

    /**
     * {@return An empty modded network.}
     */
    public static NetworkPayloadSetup empty() {
        return new NetworkPayloadSetup(Collections.emptyMap());
    }

    /**
     * {@return A modded network with the given configuration and play channels.}
     */
    public static NetworkPayloadSetup from(Map<ConnectionProtocol, NegotiationResult> results) {
        ImmutableMap.Builder<ConnectionProtocol, Map<ResourceLocation, NetworkChannel>> channels = ImmutableMap.builder();

        for (Map.Entry<ConnectionProtocol, NegotiationResult> result : results.entrySet()) {
            ImmutableMap.Builder<ResourceLocation, NetworkChannel> protocolChannels = ImmutableMap.builder();
            result.getValue().components().forEach(component -> {
                protocolChannels.put(component.id(), new NetworkChannel(component.id(), component.version()));
            });
            channels.put(result.getKey(), protocolChannels.build());
        }

        return new NetworkPayloadSetup(channels.build());
    }
}
