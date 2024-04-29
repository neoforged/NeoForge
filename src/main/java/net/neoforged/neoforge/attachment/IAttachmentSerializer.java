/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Serializer for data attachments.
 *
 * <p><b>The {@link #read(IAttachmentHolder, Tag, HolderLookup.Provider)} method must be implemented by subclasses!</b>
 *
 * @param <S> A {@link Tag} subclass: the serialized representation.
 * @param <T> The type of the data attachment.
 */
public interface IAttachmentSerializer<S extends Tag, T> {
    /**
     * Reads the attachment from NBT.
     *
     * @param holder the holder for the attachment, can be cast if the subtype is known
     * @param tag    the serialized attachment
     */
    T read(IAttachmentHolder holder, S tag, HolderLookup.Provider provider);

    /**
     * Writes the attachment to NBT, or returns null if it is should not be serialized.
     */
    @Nullable
    S write(T attachment, HolderLookup.Provider provider);
}
