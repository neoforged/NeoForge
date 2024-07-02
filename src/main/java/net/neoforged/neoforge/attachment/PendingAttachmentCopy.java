/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

public record PendingAttachmentCopy<T extends AttachmentType<D>, D, P extends IAttachmentHolder>(
        T attachmentType, IAttachmentHolder<?> currentHost, P newHost, D data) {
    /**
     * Species the reason this attachment is about to be copied.
     */
    public enum CopyReason {
        NOT_SPECIFIED,

        /**
         * Entity actually dies; not for changing dimensions.
         */
        ENTITY_DEATH,

        /**
         * A player is being cloned (typically via changing dimensions)
         */
        PLAYER_CLONE,

        /**
         * A proto-chunk is being promoted to a full chunk instance.
         */
        CHUNK_PROMOTION,

        /**
         * An entity is being converted.
         */
        CONVERTED
    }
}
