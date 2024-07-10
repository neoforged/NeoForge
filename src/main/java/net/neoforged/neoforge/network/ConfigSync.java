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
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ConfigSync {
    private ConfigSync() {}

    public static List<ConfigFilePayload> syncConfigs() {
        final Map<String, byte[]> configData = ModConfigs.getConfigSet(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> {
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        return configData.entrySet().stream()
                .map(e -> new ConfigFilePayload(e.getKey(), e.getValue()))
                .toList();
    }

    public static void receiveSyncedConfig(final byte[] contents, final String fileName) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Optional.ofNullable(ModConfigs.getFileMap().get(fileName)).ifPresent(mc -> ConfigTracker.INSTANCE.acceptSyncedConfig(mc, contents));
        }
    }
}
