/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.event;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when the server configuration packet listener collects all the configuration tasks
 * that should be run on the server to configure the client.
 */
public class OnGameConfigurationEvent extends Event implements IModBusEvent {

    private final ServerConfigurationPacketListener listener;

    private final Queue<ICustomConfigurationTask> configurationTasks = new LinkedList<>();

    @ApiStatus.Internal
    public OnGameConfigurationEvent(ServerConfigurationPacketListener listener) {
        this.listener = listener;
    }
    
    /**
     * Register a configuration task to be run on the server.
     * @param task The task to run.
     */
    public void register(ICustomConfigurationTask task) {
        configurationTasks.add(task);
    }

    /**
     * Get the configuration tasks that have been registered.
     * @return The configuration tasks.
     */
    @ApiStatus.Internal
    public Queue<ICustomConfigurationTask> getConfigurationTasks() {
        return new LinkedList<>(configurationTasks);
    }

    /**
     * Get the server configuration packet listener.
     * @return The server configuration packet listener.
     */
    public ServerConfigurationPacketListener getListener() {
        return listener;
    }
}
