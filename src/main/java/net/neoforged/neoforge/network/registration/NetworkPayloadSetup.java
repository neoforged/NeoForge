/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Set;

public record NetworkPayloadSetup(
        Set<NetworkChannel> configuration,
        Set<NetworkChannel> play,
        boolean vanilla
) {
    
    public static NetworkPayloadSetup emptyModded() {
        return new NetworkPayloadSetup(Set.of(), Set.of(), false);
    }
    
    public static NetworkPayloadSetup emptyVanilla() {
        return new NetworkPayloadSetup(Set.of(), Set.of(), true);
    }
    
    public static NetworkPayloadSetup from(Set<NetworkChannel> configuration, Set<NetworkChannel> play) {
        return new NetworkPayloadSetup(configuration, play, false);
    }
}
