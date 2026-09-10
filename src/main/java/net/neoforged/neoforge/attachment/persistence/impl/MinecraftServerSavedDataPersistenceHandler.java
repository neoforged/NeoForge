/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.persistence.impl;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.persistence.SavedDataPersistenceHandler;

public class MinecraftServerSavedDataPersistenceHandler extends SavedDataPersistenceHandler<MinecraftServer> {
    private final MinecraftServer server;

    public MinecraftServerSavedDataPersistenceHandler(MinecraftServer server) {
        this.server = server;
    }

    @Override
    protected RegistryAccess registryAccess() {
        return server.registryAccess();
    }

    @Override
    protected SavedDataPersistenceHandler<MinecraftServer> createInstance() {
        return new MinecraftServerSavedDataPersistenceHandler(server);
    }
}
