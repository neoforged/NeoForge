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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.event.config.ModConfigEvent;
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
        var connection = listener.getConnection();
        if (connection.isMemoryConnection()) {
            return; // Do not sync server configs with ourselves
        }

        synchronized (lock) {
            configsToSync.put(connection, new LinkedHashMap<>());
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

    /**
     * Registers a listener for {@link ModConfigEvent.Reloading} for all mod busses,
     * that will sync changes to server configs to connected clients.
     */
    public static void registerEventListeners() {
        for (var modContainer : ModList.get().getSortedMods()) {
            var modBus = modContainer.getEventBus();
            if (modBus != null) {
                modBus.addListener(ModConfigEvent.Reloading.class, event -> {
                    var config = event.getConfig();
                    if (config.getType() != ModConfig.Type.SERVER) {
                        return;
                    }
                    var loadedConfig = config.getLoadedConfig();
                    if (loadedConfig == null) {
                        return;
                    }

                    var configFormat = loadedConfig.config().configFormat();
                    if (configFormat.isInMemory()) {
                        return; // An in-memory format indicates that we received this config from a server and shouldn't try to sync it
                    }

                    // Write config bytes and queue for syncing to all connected players.
                    var bytes = configFormat.createWriter().writeToString(loadedConfig.config()).getBytes(StandardCharsets.UTF_8);
                    synchronized (lock) {
                        for (var toSync : configsToSync.values()) {
                            toSync.put(config.getFileName(), bytes);
                        }
                    }
                });
            }
        }
    }

    public static void syncPendingConfigs(MinecraftServer server) {
        synchronized (lock) {
            for (var player : server.getPlayerList().getPlayers()) {
                if (!player.connection.hasChannel(ConfigFilePayload.TYPE)) {
                    continue; // Only sync configs to NeoForge clients supporting config sync
                }

                if (player.connection.getConnection().isMemoryConnection()) {
                    continue; // Do not sync server configs with ourselves
                }

                var toSync = configsToSync.get(player.connection.getConnection());
                if (toSync == null) {
                    // null for GameTestPlayer. Should not happen for normal players though.
                    if (player.getClass() == ServerPlayer.class) {
                        throw new IllegalStateException("configsToSync should contain an entry for player " + player.getName());
                    } else {
                        continue;
                    }
                }
                toSync.forEach((fileName, data) -> {
                    PacketDistributor.sendToPlayer(player, new ConfigFilePayload(fileName, data));
                });
                toSync.clear();
            }
        }
    }

    public static void receiveSyncedConfig(final byte[] contents, final String fileName) {
        Optional.ofNullable(ModConfigs.getFileMap().get(fileName)).ifPresent(mc -> ConfigTracker.acceptSyncedConfig(mc, contents));
    }
}
