/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Serializer for data attachments.
 *
 * <p><b>The {@link #read(IAttachmentHolder, Tag, HolderLookup.Provider)} method must be implemented by subclasses!</b>
 *
 * @param <T> The type of the data attachment.
 */
public interface IAttachmentSerializer<T> {
    /**
     * Reads the attachment from NBT.
     *
     * @param holder the holder for the attachment, can be cast if the subtype is known
     * @param tag    the serialized attachment
     */
    T read(IAttachmentHolder holder, ValueInput input, HolderLookup.Provider provider);

    /**
     * Writes the attachment to NBT, or returns {@code false} if it is should not be serialized.
     */
    boolean write(T attachment, ValueOutput output, HolderLookup.Provider provider);
}
