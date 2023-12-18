/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

/**
 * Represents a complete negotiated network, which is stored on the client and server.
 *
 * @param configuration The configuration channels.
 * @param play The play channels.
 * @param vanilla Whether this is a vanilla network.
 */
@ApiStatus.Internal
public record NetworkPayloadSetup(
        Set<NetworkChannel> configuration,
        Set<NetworkChannel> play,
        boolean vanilla) {
    
    /**
     * {@return An empty modded network.}
     */
    public static NetworkPayloadSetup emptyModded() {
        return new NetworkPayloadSetup(Set.of(), Set.of(), false);
    }

    /**
     * {@return An empty vanilla network.}
     */
    public static NetworkPayloadSetup emptyVanilla() {
        return new NetworkPayloadSetup(Set.of(), Set.of(), true);
    }

    /**
     * {@return A modded network with the given configuration and play channels.}
     */
    public static NetworkPayloadSetup from(Set<NetworkChannel> configuration, Set<NetworkChannel> play) {
        return new NetworkPayloadSetup(configuration, play, false);
    }
}
