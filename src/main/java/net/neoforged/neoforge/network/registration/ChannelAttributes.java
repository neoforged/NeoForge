/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.CommonRegisterPayload;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkQueryPayload;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for manipulation of Netty {@link Channel} attributes
 */
@ApiStatus.Internal
public class ChannelAttributes {
    /**
     * Negotiated modded payload setup from {@link ModdedNetworkQueryPayload} and/or {@link ModdedNetworkPayload}.
     */
    public static final AttributeKey<NetworkPayloadSetup> PAYLOAD_SETUP = AttributeKey.valueOf("neoforge:payload_setup");

    /**
     * Known ad-hoc channels from {@link MinecraftRegisterPayload}.
     */
    public static final AttributeKey<Set<ResourceLocation>> ADHOC_CHANNELS = AttributeKey.valueOf("neoforge:adhoc_channels");

    /**
     * Known common channels from {@link CommonRegisterPayload}.
     */
    public static final AttributeKey<Map<ConnectionProtocol, Set<ResourceLocation>>> COMMON_CHANNELS = AttributeKey.valueOf("neoforge:common_channels");

    /**
     * The {@link ConnectionType} of the current connection
     */
    public static final AttributeKey<ConnectionType> CONNECTION_TYPE = AttributeKey.valueOf("neoforge:connection_type");

    @Nullable
    public static NetworkPayloadSetup getPayloadSetup(Connection connection) {
        return connection.channel().attr(PAYLOAD_SETUP).get();
    }

    public static void setPayloadSetup(Connection connection, NetworkPayloadSetup setup) {
        connection.channel().attr(PAYLOAD_SETUP).set(setup);
    }

    @Nullable
    public static ConnectionType getConnectionType(Connection connection) {
        return connection.channel().attr(CONNECTION_TYPE).get();
    }

    public static void setConnectionType(Connection connection, ConnectionType type) {
        connection.channel().attr(CONNECTION_TYPE).set(type);
    }

    /**
     * Returns a mutable set of the currently known ad-hoc channels.
     */
    public static Set<ResourceLocation> getOrCreateAdHocChannels(Connection connection) {
        Set<ResourceLocation> channels = connection.channel().attr(ADHOC_CHANNELS).get();

        if (channels == null) {
            channels = new HashSet<>();
            connection.channel().attr(ADHOC_CHANNELS).set(channels);
        }

        return channels;
    }

    /**
     * Returns a mutable set of the currently known common channels for the given protocol.
     */
    public static Set<ResourceLocation> getOrCreateCommonChannels(Connection connection, ConnectionProtocol protocol) {
        Map<ConnectionProtocol, Set<ResourceLocation>> channels = connection.channel().attr(COMMON_CHANNELS).get();

        if (channels == null) {
            channels = new EnumMap<>(ConnectionProtocol.class);
            connection.channel().attr(COMMON_CHANNELS).set(channels);
        }

        return channels.computeIfAbsent(protocol, p -> new HashSet<>());
    }
}
