/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

public enum ConnectionType {
    VANILLA,
    OTHER,
    NEOFORGE;

    public ConnectionType withMinecraftRegisterPayload() {
        return this == VANILLA ? OTHER : this;
    }

    public ConnectionType withNeoForgeQueryPayload() {
        return NEOFORGE;
    }

    public boolean isVanilla() {
        return this == VANILLA;
    }

    public boolean isNotVanilla() {
        return !isVanilla();
    }

    public boolean isOther() {
        return this == OTHER;
    }

    public boolean isNeoForge() {
        return this == NEOFORGE;
    }
}
