/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.jsonrpc.internalapi.MinecraftApi;

/// Extension interface for [MinecraftApi]
public interface IMinecraftApiExtension {
    default RegistryAccess registryAccess() {
        return self().server().registryAccess();
    }

    private MinecraftApi self() {
        return (MinecraftApi) this;
    }
}
