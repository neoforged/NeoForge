/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event.lifecycle;

import net.minecraft.client.Minecraft;

/**
 * Emitted right before the client ticks for the first time.
 * <p>
 * The loading overlay will still be showing and the resource reload is likely still going on.
 */
public class ClientStartedEvent extends ClientLifecycleEvent {
    public ClientStartedEvent(Minecraft client) {
        super(client);
    }
}
