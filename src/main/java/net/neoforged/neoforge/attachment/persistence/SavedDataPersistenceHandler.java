/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public abstract class SavedDataPersistenceHandler<T extends IAttachmentHolder>
        extends SavedData implements AttachmentHolderPersistenceHandler {
    private final Codec<SavedDataPersistenceHandler<T>> CODEC;
    protected final SavedDataType<SavedDataPersistenceHandler<T>> TYPE;

    public SavedDataPersistenceHandler() {
        CODEC = this.makeCodec();
        TYPE = new SavedDataType<>(AttachmentPersistence.IDENTIFIER, this::createInstance, CODEC);
    }

    private Codec<SavedDataPersistenceHandler<T>> makeCodec() {
        return CompoundTag.CODEC.flatXmap(tag -> {
            var data = createInstance();
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            // Note: Side effect here, keep an eye on this
            data.deserialize(TagValueInput.create(reporter, registryAccess(), tag));
            return !reporter.isEmpty()
                    ? DataResult.error(() -> "Deserialisation error in level attachments: " + reporter.getReport())
                    : DataResult.success(data);
        }, data -> {
            ProblemReporter.Collector reporter = new ProblemReporter.Collector();
            var tag = TagValueOutput.createWithContext(reporter, registryAccess());
            data.serialize(tag);
            return !reporter.isEmpty()
                    ? DataResult.error(() -> "Serialisation error in level attachments: " + reporter.getReport())
                    : DataResult.success(tag.buildResult());
        });
    }

    @Override
    public void serialize(ValueOutput output) {
        output.store(AttachmentPersistence.ATTACHMENTS_NBT_KEY, CODEC, this);
    }

    @Override
    public void deserialize(ValueInput input) {
        input.read(AttachmentPersistence.ATTACHMENTS_NBT_KEY, CODEC);
    }

    protected abstract RegistryAccess registryAccess();

    protected abstract SavedDataPersistenceHandler<T> createInstance();

    @Override
    public boolean isDirty() {
        // Always re-save
        return true;
    }
}
