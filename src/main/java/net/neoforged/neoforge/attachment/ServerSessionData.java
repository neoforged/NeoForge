/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * Session Data for a server. Saves through {@link MinecraftServer#getDataStorage()}.
 */
@ApiStatus.Internal
public final class ServerSessionData extends SessionData {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "session_attachments");

    private final MinecraftServer server;
    private final SavedDataType<Storage> savedDataType;

    @VisibleForTesting
    public ServerSessionData(MinecraftServer server) {
        this.server = server;
        Codec<Storage> codec = makeCodec();
        this.savedDataType = new SavedDataType<>(ID, level -> new Storage(), level -> codec);
    }

    /**
     * Creates the holder and loads it from the server's data storage, which then keeps it registered for saving.
     */
    public static ServerSessionData create(MinecraftServer server) {
        // This is called once during server startup, so creating a new instance and always returning it is fine.
        var data = new ServerSessionData(server);
        server.getDataStorage().computeIfAbsent(data.savedDataType);
        return data;
    }

    public SavedDataType<? extends SavedData> savedDataType() {
        return savedDataType;
    }

    private Codec<Storage> makeCodec() {
        return CompoundTag.CODEC.flatXmap(tag -> {
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            // Note: Side effect here, keep an eye on this
            deserializeAttachments(TagValueInput.create(reporter, server.registryAccess(), tag));
            return !reporter.isEmpty()
                    ? DataResult.error(() -> "Deserialisation error in session attachments: " + reporter.getReport())
                    : DataResult.success(new Storage());
        }, storage -> {
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            var tag = TagValueOutput.createWithContext(reporter, server.registryAccess());
            serializeAttachments(tag);
            return !reporter.isEmpty()
                    ? DataResult.error(() -> "Serialisation error in session attachments: " + reporter.getReport())
                    : DataResult.success(tag.buildResult());
        });
    }

    @Override
    public void syncData(AttachmentType<?> type) {
        AttachmentSync.syncSessionUpdate(server, this, type);
    }

    // Marker: the data lives in the enclosing holder
    private static final class Storage extends SavedData {
        @Override
        public boolean isDirty() {
            // Always re-save
            return true;
        }
    }
}
