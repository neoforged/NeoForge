/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

/**
 * Custom copy handler for data attachments, to improve efficiency compared to the default
 * serialize-deserialize-implementation.
 */
public interface IAttachmentCopyHandler<T> {
    /**
     * creates a copy of the attachment. The copy should be equal to serializing and deserializing the attachment.
     * 
     * @param holder     the holder the attachment will be part of after copying
     * @param attachment the attachment to copy
     * @return the copy or null if it shouldn't be copied.
     */
    @Nullable
    T copy(HolderLookup.Provider provider, IAttachmentHolder holder, T attachment);
}
