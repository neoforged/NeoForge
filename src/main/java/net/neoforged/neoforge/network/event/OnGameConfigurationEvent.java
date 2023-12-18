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

public class OnGameConfigurationEvent extends Event implements IModBusEvent {

    private final ServerConfigurationPacketListener listener;

    private final Queue<ICustomConfigurationTask> configurationTasks = new LinkedList<>();

    public OnGameConfigurationEvent(ServerConfigurationPacketListener listener) {
        this.listener = listener;
    }

    public void register(ICustomConfigurationTask task) {
        configurationTasks.add(task);
    }

    public Queue<ICustomConfigurationTask> getConfigurationTasks() {
        return new LinkedList<>(configurationTasks);
    }

    public ServerConfigurationPacketListener getListener() {
        return listener;
    }
}
