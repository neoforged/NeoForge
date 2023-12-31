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
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ConfigSync {
    public static final ConfigSync INSTANCE = new ConfigSync(ConfigTracker.INSTANCE);
    private final ConfigTracker tracker;

    private ConfigSync(final ConfigTracker tracker) {
        this.tracker = tracker;
    }

    public List<ConfigFilePayload> syncConfigs() {
        final Map<String, byte[]> configData = tracker.configSets().get(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> {
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        return configData.entrySet().stream()
                .map(e -> new ConfigFilePayload(e.getValue(), e.getKey()))
                .toList();
    }

    public void receiveSyncedConfig(final byte[] contents, final String fileName) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Optional.ofNullable(tracker.fileMap().get(fileName)).ifPresent(mc -> mc.acceptSyncedConfig(contents));
        }
    }
}
