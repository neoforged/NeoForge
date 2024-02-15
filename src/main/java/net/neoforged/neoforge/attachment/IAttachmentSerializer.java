/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Serializer for data attachments.
 *
 * <p><b>The {@link #read(IAttachmentHolder, Tag)} method must be implemented by subclasses!</b>
 *
 * @param <S> A {@link Tag} subclass: the serialized representation.
 * @param <T> The type of the data attachment.
 */
public interface IAttachmentSerializer<S extends Tag, T> {
    /**
     * @deprecated Implement {@link #read(IAttachmentHolder, Tag)} instead.
     *             This method will be removed in a future version.
     */
    @Deprecated(forRemoval = true, since = "1.20.4")
    default T read(S tag) {
        throw new RuntimeException("IAttachmentSerializer must implement the read() that accepts a holder!");
    }

    /**
     * Reads the attachment from NBT.
     *
     * <p>In a future version, the default implementation will be removed,
     * but for now it exists for backwards compatibility with {@link #read(Tag)}.
     *
     * @param holder the holder for the attachment, can be cast if the subtype is known
     * @param tag    the serialized attachment
     */
    default T read(IAttachmentHolder holder, S tag) {
        return read(tag);
    }

    /**
     * Writes the attachment to NBT, or returns null if it is should not be serialized.
     */
    @Nullable
    S write(T attachment);
}
