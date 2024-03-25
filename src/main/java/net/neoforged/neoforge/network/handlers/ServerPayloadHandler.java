/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handlers;

import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ServerPayloadHandler {
    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    private ServerPayloadHandler() {}

    public void handle(FrozenRegistrySyncCompletedPayload payload, ConfigurationPayloadContext context) {
        context.taskCompletedHandler().onTaskCompleted(SyncRegistries.TYPE);
    }
}
