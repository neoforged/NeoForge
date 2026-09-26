/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import java.util.Objects;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;

/// Extension interface for [MinecraftApi]
public interface IMinecraftApiExtension {
    default DedicatedServer server() {
        return Objects.requireNonNull(self().notificationManager().server());
    }

    default RegistryAccess registryAccess() {
        return server().registryAccess();
    }

    private MinecraftApi self() {
        return (MinecraftApi) this;
    }
}
