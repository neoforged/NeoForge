/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.payload.ModdedNetworkPayload;

/**
 * Declares categories of connections based on the other side.
 */
public enum ConnectionType {
    /**
     * Indicates that the other end is Neo.
     * Modded channels will be negotiated via {@link ModdedNetworkPayload}.
     */
    NEOFORGE,

    /**
     * Indicates that the other end of the connection is not Neo. This may be a Vanilla connection, or another modded platform (such as Fabric or Bukkit).
     * Modded channels will be negotiated via {@link MinecraftRegisterPayload}.
     */
    OTHER;

    public boolean isOther() {
        return this == OTHER;
    }

    public boolean isNeoForge() {
        return this == NEOFORGE;
    }
}
