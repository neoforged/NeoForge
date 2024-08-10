/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.network.configuration.SyncConfig;
import net.neoforged.neoforge.network.payload.ConfigFilePayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ConfigSync {
    private static final Object lock = new Object();
    /**
     * Connection -> Config file name -> byte[] of the config serialized to TOML.
     *
     * <p>Pending config updates get sent to players in the PLAY phase only,
     * but start being tracked as soon as the {@link SyncConfig} configuration task runs.
     * This ensures that all updates during the configuration phase will eventually arrive to the clients.
     *
     * <p>Connections get removed when GC'ed thanks to the WeakHashMap.
     */
    private static final Map<Connection, Map<String, byte[]>> configsToSync = new WeakHashMap<>();

    private ConfigSync() {}

    public static void syncAllConfigs(ServerConfigurationPacketListener listener) {
        synchronized (lock) {
            configsToSync.put(listener.getConnection(), new LinkedHashMap<>());
        }

        final Map<String, byte[]> configData = ModConfigs.getConfigSet(ModConfig.Type.SERVER).stream().collect(Collectors.toMap(ModConfig::getFileName, mc -> {
            try {
                return Files.readAllBytes(mc.getFullPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        for (var entry : configData.entrySet()) {
            listener.send(new ConfigFilePayload(entry.getKey(), entry.getValue()));
        }
    }

    public static void addPendingConfig(ModConfig modConfig, IConfigSpec.ILoadedConfig loadedConfig) {
        var bytes = loadedConfig.config().configFormat().createWriter().writeToString(loadedConfig.config()).getBytes(StandardCharsets.UTF_8);
        synchronized (lock) {
            for (var toSync : configsToSync.values()) {
                toSync.put(modConfig.getFileName(), bytes);
            }
        }
    }

    public static void syncPendingConfigs(MinecraftServer server) {
        synchronized (lock) {
            for (var player : server.getPlayerList().getPlayers()) {
                var toSync = configsToSync.get(player.connection.getConnection());
                if (toSync == null) {
                    throw new IllegalStateException("configsToSync should contain an entry for player " + player.getName());
                }
                toSync.forEach((fileName, data) -> {
                    PacketDistributor.sendToPlayer(player, new ConfigFilePayload(fileName, data));
                });
                toSync.clear();
            }
        }
    }

    public static void receiveSyncedConfig(final byte[] contents, final String fileName) {
        if (!Minecraft.getInstance().isLocalServer()) {
            Optional.ofNullable(ModConfigs.getFileMap().get(fileName)).ifPresent(mc -> ConfigTracker.INSTANCE.acceptSyncedConfig(mc, contents));
        }
    }
}
