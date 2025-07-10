/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Serializer for data attachments.
 *
 * @param <T> The type of the data attachment.
 */
public interface IAttachmentSerializer<T> {
    /**
     * Reads the attachment from NBT.
     *
     * @param holder the holder for the attachment, can be cast if the subtype is known
     * @param input  the input to read from
     */
    T read(IAttachmentHolder holder, ValueInput input);

    /**
     * Writes the attachment to the value output, or returns {@code false} if it is should not be serialized.
     *
     * <p>If {@code false} is returned, any data written to the value output by this method will be discarded.
     */
    boolean write(T attachment, ValueOutput output);
}
