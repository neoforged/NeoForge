/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
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
        return CompoundTag.CODEC.flatXmap(tag -> {
            var data = new LevelAttachmentsSavedData(context);
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            // Note: Side effect here, keep an eye on this
            data.level.deserializeAttachments(TagValueInput.create(reporter, data.level.registryAccess(), tag));
            return !reporter.isEmpty()
                    ? DataResult.error(() -> "Deserialisation error in level attachments: " + reporter.getReport())
                    : DataResult.success(data);
        }, data -> {
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            var tag = TagValueOutput.createWithContext(reporter, data.level.registryAccess());
            data.level.serializeAttachments(tag);
            return !reporter.isEmpty()
                    ? DataResult.error(() -> "Serialisation error in level attachments: " + reporter.getReport())
                    : DataResult.success(tag.buildResult());
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
