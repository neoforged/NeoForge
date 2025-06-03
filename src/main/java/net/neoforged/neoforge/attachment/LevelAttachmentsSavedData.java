/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class LevelAttachmentsSavedData extends SavedData {
    public static final SavedDataType<LevelAttachmentsSavedData> TYPE = new SavedDataType<>(
            "neoforge_data_attachments",
            LevelAttachmentsSavedData::new,
            LevelAttachmentsSavedData::makeCodec);

    public static void init(ServerLevel level) {
        // Querying the attachment a single time is enough to initialize it,
        // and make sure it gets saved when the level is saved.
        level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static Codec<LevelAttachmentsSavedData> makeCodec(SavedData.Context context) {
        return CompoundTag.CODEC.xmap(tag -> {
            var data = new LevelAttachmentsSavedData(context);
            // Note: Side effect here, keep an eye on this
            data.level.deserializeAttachments(data.level.registryAccess(), tag);
            return data;
        }, data -> {
            // Make sure we don't return null
            return Objects.requireNonNullElseGet(data.level.serializeAttachments(data.level.registryAccess()), CompoundTag::new);
        });
    }

    private final ServerLevel level;

    public LevelAttachmentsSavedData(SavedData.Context context) {
        this.level = context.levelOrThrow();
    }

    @Override
    public boolean isDirty() {
        // Always re-save
        return true;
    }
}
