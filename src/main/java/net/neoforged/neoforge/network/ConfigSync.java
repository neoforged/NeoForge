/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.simple.MessageFunctions;

public class ConfigSync {
    public static final ConfigSync INSTANCE = new ConfigSync(ConfigTracker.INSTANCE);
    private final ConfigTracker tracker;

    private ConfigSync(final ConfigTracker tracker) {
        this.tracker = tracker;
    }

    public List<MessageFunctions.LoginPacket<HandshakeMessages.S2CConfigData>> syncConfigs(boolean isLocal) {
        final Map<String, byte[]> configData = tracker.configSets().get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> {
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return configData.entrySet().stream().map(e -> new MessageFunctions.LoginPacket<>("Config " + e.getKey(), new HandshakeMessages.S2CConfigData(e.getKey(), e.getValue()))).toList();
    }

    public void receiveSyncedConfig(final HandshakeMessages.S2CConfigData s2CConfigData, final NetworkEvent.Context contextSupplier) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Optional.ofNullable(tracker.fileMap().get(s2CConfigData.getFileName())).ifPresent(mc -> mc.acceptSyncedConfig(s2CConfigData.getBytes()));
        }
    }
}
