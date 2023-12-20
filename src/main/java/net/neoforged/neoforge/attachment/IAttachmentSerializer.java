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
public interface IAttachmentSerializer<S extends Tag, T> {
    T read(S tag);

    S write(T attachment);
}
