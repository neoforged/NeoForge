/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.event;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when the server configuration packet listener collects all the configuration tasks
 * that should be run on the server to configure the client.
 */
public class RegisterConfigurationTasksEvent extends Event implements IModBusEvent {
    private final ServerConfigurationPacketListener listener;

    private final Queue<ConfigurationTask> configurationTasks = new LinkedList<>();

    @ApiStatus.Internal
    public RegisterConfigurationTasksEvent(ServerConfigurationPacketListener listener) {
        this.listener = listener;
    }

    /**
     * Register a configuration task to be run on the server.
     * <p>
     * If you need to send payloads during your task, extend {@link ICustomConfigurationTask} instead of {@link ConfigurationTask}.
     * 
     * @param task The task to run.
     */
    public void register(ConfigurationTask task) {
        configurationTasks.add(task);
    }

    /**
     * Get the configuration tasks that have been registered.
     * 
     * @return The configuration tasks.
     */
    @ApiStatus.Internal
    public Queue<ConfigurationTask> getConfigurationTasks() {
        return new LinkedList<>(configurationTasks);
    }

    /**
     * Get the server configuration packet listener.
     * 
     * @return The server configuration packet listener.
     */
    public ServerConfigurationPacketListener getListener() {
        return listener;
    }
}
