/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import javax.annotation.Nullable;

/**
 * Represents an object that supports {@link AttachmentType registry entry attachments}.
 *
 * @param <T> the type of the self object
 * @see #getAttachment(AttachmentTypeKey)
 */
public interface IWithAttachments<T>
{
    /**
     * {@return the attached value of the given {@code type}, or {@code null} if one doesn't exist}
     *
     * @param type the key of the attachment to query
     * @param <A>  the type of the attached value
     */
    default <A> @Nullable A getAttachment(AttachmentTypeKey<A> type)
    {
        return null;
    }
}
