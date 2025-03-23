/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event.lifecycle;

import net.minecraft.client.Minecraft;

/**
 * Called after {@link ClientStoppingEvent} when the client has completely shut down.
 * Called immediately before shutting down.
 */
public class ClientStoppedEvent extends ClientLifecycleEvent {
    public ClientStoppedEvent(Minecraft client) {
        super(client);
    }
}
