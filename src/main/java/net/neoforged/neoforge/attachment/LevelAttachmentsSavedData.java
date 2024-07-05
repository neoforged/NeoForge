/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class LevelAttachmentsSavedData extends SavedData {
    private static final String NAME = "neoforge_data_attachments";

    public static void init(ServerLevel level) {
        var factory = new SavedData.Factory<>(
                () -> new LevelAttachmentsSavedData(level),
                (tag, prov) -> new LevelAttachmentsSavedData(level, tag));
        // Querying the attachment a single time is enough to initialize it,
        // and make sure it gets saved when the level is saved.
        level.getDataStorage().computeIfAbsent(factory, NAME);
    }

    private final ServerLevel level;

    public LevelAttachmentsSavedData(ServerLevel level) {
        this.level = level;
    }

    public LevelAttachmentsSavedData(ServerLevel level, CompoundTag tag) {
        this.level = level;
        level.deserializeAttachments(level.registryAccess(), tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        // Make sure we don't return null
        return Objects.requireNonNullElseGet(level.serializeAttachments(provider), CompoundTag::new);
    }

    @Override
    public boolean isDirty() {
        // Always re-save
        return true;
    }
}
