/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event.lifecycle;

import net.minecraft.client.Minecraft;

/**
 * Called after {@link ClientStoppingEvent} when the server has completely shut down.
 * Called immediately before shutting down, on the dedicated server, and before returning
 * to the main menu on the client.
 */
public class ClientStoppedEvent extends ClientLifecycleEvent {
    public ClientStoppedEvent(Minecraft client) {
        super(client);
    }
}
