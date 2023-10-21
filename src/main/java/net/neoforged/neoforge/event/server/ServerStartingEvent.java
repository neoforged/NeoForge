/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Called after {@link ServerAboutToStartEvent} and before {@link ServerStartedEvent}.
 * This event allows for customizations of the server.
 *
 * If you need to add commands use {@link RegisterCommandsEvent}.
 *
 * @author cpw
 */
public class ServerStartingEvent extends ServerLifecycleEvent
{
    public ServerStartingEvent(final MinecraftServer server)
    {
        super(server);
    }

}
