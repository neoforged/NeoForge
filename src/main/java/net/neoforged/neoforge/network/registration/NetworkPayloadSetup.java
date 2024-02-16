/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a complete negotiated network, which is stored on the client and server.
 *
 * @param configuration The configuration channels.
 * @param play          The play channels.
 */
@ApiStatus.Internal
public record NetworkPayloadSetup(
        Map<ResourceLocation, NetworkChannel> configuration,
        Map<ResourceLocation, NetworkChannel> play) {
    /**
     * {@return An empty modded network.}
     */
    public static NetworkPayloadSetup empty() {
        return new NetworkPayloadSetup(Map.of(), Map.of());
    }

    /**
     * {@return A modded network with the given configuration and play channels.}
     */
    public static NetworkPayloadSetup from(Set<NetworkChannel> configuration, Set<NetworkChannel> play) {
        return new NetworkPayloadSetup(
                configuration.stream().collect(Collectors.toMap(NetworkChannel::id, Function.identity())),
                play.stream().collect(Collectors.toMap(NetworkChannel::id, Function.identity())));
    }
}
