/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.attachment;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.attachment.AttachmentInternals;
import net.neoforged.neoforge.attachment.SessionData;
import net.neoforged.neoforge.common.IOUtilities;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Session data for the client.
 * <p>
 * Persistent attachments that aren't synced are saved by the client using a per-(world/server/lan-world) path.
 * Synced attachments are server authoritative and are never saved on the client.
 */
@ApiStatus.Internal
public final class ClientSessionData extends SessionData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SAVE_INTERVAL_TICKS = 6000; // Same as the server autosave
    private static final String DATA_KEY = "data";
    private static final int MAX_KEY_LENGTH = 64; // Keeps file names well within file system limits
    private static final int HASH_LENGTH = 8;
    static final String SINGLEPLAYER_FILE = "neoforge/client_session_attachments.dat"; // Relative to the world's data directory
    static final String MULTIPLAYER_DIR = "neoforge/client_session_attachments"; // Relative to the game directory

    private final RegistryAccess registryAccess;
    private final Path file;
    private int ticksUntilSave = SAVE_INTERVAL_TICKS;

    public ClientSessionData(RegistryAccess registryAccess, Path file) {
        this.registryAccess = registryAccess;
        this.file = file;
    }

    public static ClientSessionData create(Minecraft minecraft, ClientPacketListener listener) {
        Path file = resolveFile(minecraft.gameDirectory.toPath(), minecraft.getSingleplayerServer(), listener.getServerData());
        var data = new ClientSessionData(listener.registryAccess(), file);
        data.load();
        return data;
    }

    /**
     * World file for the singleplayer host, game directory file keyed by server otherwise.
     *
     * @throws IllegalStateException If neither the integrated server nor the server data are provided.
     */
    public static Path resolveFile(Path gameDirectory, @Nullable IntegratedServer singleplayerServer, @Nullable ServerData serverData) {
        if (serverData != null) {
            return gameDirectory.resolve(MULTIPLAYER_DIR).resolve(storageKey(serverData) + ".dat");
        }
        if (singleplayerServer != null) {
            return singleplayerServer.getWorldPath(LevelResource.DATA).resolve(SINGLEPLAYER_FILE);
        }
        throw new IllegalStateException("Connection has neither a singleplayer world nor server data");
    }

    /**
     * Realms and LAN games have no stable address, so they are keyed by name instead.
     * Keys do not survive renames or host IP changes, and a server transfer keeps the origin server's key.
     * Keys longer than {@value #MAX_KEY_LENGTH} characters are clamped and suffixed with a hash of the original key.
     */
    @SuppressWarnings("deprecation")
    public static String storageKey(ServerData serverData) {
        String key = switch (serverData.type()) {
            case REALM -> "realm_" + serverData.name;
            case LAN -> "lan_" + ServerAddress.parseString(serverData.ip).getHost() + "_" + serverData.name;
            case OTHER -> {
                if (!ServerAddress.isValidAddress(serverData.ip)) {
                    yield serverData.ip;
                }
                ServerAddress address = ServerAddress.parseString(serverData.ip);
                yield address.getHost() + "_" + address.getPort();
            }
        };
        // Names can contain anything, including path separators. They might also be too long for Windows path limits.
        String sanitized = Util.sanitizeName(key, c -> c != '/' && Identifier.validPathChar(c));
        if (sanitized.length() <= MAX_KEY_LENGTH) {
            return sanitized;
        }
        String hash = Hashing.sha1().hashUnencodedChars(key).toString().substring(0, HASH_LENGTH);
        return sanitized.substring(0, MAX_KEY_LENGTH - HASH_LENGTH - 1) + "-" + hash;
    }

    public void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
            CompoundTag tag = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            AttachmentInternals.deserializeNonSyncedAttachments(this, TagValueInput.create(reporter, registryAccess, tag.getCompoundOrEmpty(DATA_KEY)));
        } catch (Exception exception) {
            LOGGER.error("Failed to read client session attachments from {}", file, exception);
        }
    }

    public void save() {
        try (var reporter = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registryAccess);
            AttachmentInternals.serializeNonSyncedAttachments(this, output);
            CompoundTag data = output.buildResult();
            // Do not create the file for empty data, but keep an existing one so that removals persist
            if (data.isEmpty() && !Files.exists(file)) {
                return;
            }
            CompoundTag tag = new CompoundTag();
            tag.put(DATA_KEY, data);
            NbtUtils.addCurrentDataVersion(tag);
            Files.createDirectories(file.getParent());
            IOUtilities.writeNbtCompressed(tag, file);
        } catch (Exception exception) {
            LOGGER.error("Failed to write client session attachments to {}", file, exception);
        }
    }

    public void tick() {
        if (--ticksUntilSave <= 0) {
            ticksUntilSave = SAVE_INTERVAL_TICKS;
            save();
        }
    }

    private ProblemReporter.PathElement problemPath() {
        return () -> "client session attachments " + file;
    }
}
