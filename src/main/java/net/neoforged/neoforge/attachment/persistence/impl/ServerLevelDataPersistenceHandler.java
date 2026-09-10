/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.persistence.impl;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.attachment.persistence.SavedDataPersistenceHandler;

public class ServerLevelDataPersistenceHandler extends SavedDataPersistenceHandler<ServerLevel> {
    private final ServerLevel level;

    public ServerLevelDataPersistenceHandler(ServerLevel level) {
        this.level = level;
        level.getDataStorage().computeIfAbsent(TYPE);
    }

    @Override
    protected RegistryAccess registryAccess() {
        return level.registryAccess();
    }

    @Override
    protected ServerLevelDataPersistenceHandler createInstance() {
        return new ServerLevelDataPersistenceHandler(level);
    }
}
