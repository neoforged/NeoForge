/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.nbt.Tag;

/**
 * Serializer for data attachments.
 *
 * @param <S> A {@link Tag} subclass: the serialized representation.
 * @param <T> The type of the data attachment.
 */
public interface IAttachmentSerializer<H, S extends Tag, T> {
    /**
     * Reads the attachment from NBT.
     *
     * @param holder the holder for the attachment, can be cast if the subtype is known
     * @param tag    the serialized attachment
     */
    T read(H holder, S tag);

    /**
     * Writes the attachment to NBT.
     */
    S write(T attachment);
}
