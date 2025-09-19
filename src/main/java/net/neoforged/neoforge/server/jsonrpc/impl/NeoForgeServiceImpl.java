/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc.impl;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.server.jsonrpc.NeoForgeService;
import net.neoforged.neoforgespi.language.IModInfo;

public class NeoForgeServiceImpl implements NeoForgeService {
    private final DedicatedServer server;
    private final JsonRpcLogger logger;

    public NeoForgeServiceImpl(DedicatedServer server, JsonRpcLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public List<IModInfo> getModList() {
        return ModList.get().getMods();
    }

    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistries() {
        return server.registryAccess().listRegistryKeys();
    }

    @Override
    public <T> Stream<ResourceKey<T>> listRegistryKeys(ResourceKey<? extends Registry<T>> registryKey) {
        return server.registryAccess().lookupOrThrow(registryKey).listElementIds();
    }
}
