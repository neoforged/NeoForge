/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event.lifecycle;

import net.minecraft.client.Minecraft;

/**
 * Called when the client begins an orderly shutdown, before {@link ClientStoppedEvent}.
 */
public class ClientStoppingEvent extends ClientLifecycleEvent {
    public ClientStoppingEvent(Minecraft client) {
        super(client);
    }
}
