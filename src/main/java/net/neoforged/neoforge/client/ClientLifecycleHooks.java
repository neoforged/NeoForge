/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppedEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ClientLifecycleHooks {
    /**
     * Emits the ClientStartedEvent right before the client ticks for the first time.
     */
    public static void handleClientStarted(Minecraft minecraft) {
        NeoForge.EVENT_BUS.post(new ClientStartedEvent(minecraft));
    }

    /**
     * Emits the ClientStoppingEvent when the client is about to free all of its resources.
     */
    public static void handleClientStopping(Minecraft minecraft) {
        NeoForge.EVENT_BUS.post(new ClientStoppingEvent(minecraft));
    }

    /**
     * Emits the ClientStoppedEvent right after the client has freed all of its resources
     * and is about to shut down the process.
     */
    public static void handleClientStopped(Minecraft minecraft) {
        NeoForge.EVENT_BUS.post(new ClientStoppedEvent(minecraft));
    }
}
