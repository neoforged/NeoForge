/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

/**
 * Extension class for {@link ServerConfigurationPacketListener}
 */
public interface IServerConfigurationPacketListenerExtension extends IServerCommonPacketListenerExtension {
    /**
     * Call when a configuration task is finished
     *
     * @param task The task that was finished
     * @implNote This forces the normally private method implementation in {@link ServerConfigurationPacketListenerImpl#finishCurrentTask(ConfigurationTask.Type)} to become public, and adds this to the signature of {@link ServerConfigurationPacketListener}
     */
    void finishCurrentTask(ConfigurationTask.Type task);
}
