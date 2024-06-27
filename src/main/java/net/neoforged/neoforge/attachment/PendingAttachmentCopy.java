/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

public record PendingAttachmentCopy<T extends AttachmentType<D>, D, P extends IAttachmentHolder>(
        T attachmentType, IAttachmentHolder currentHost, P newHost, D data) {
    public enum CopyReason {
        NOT_SPECIFIED, DEATH, CHUNK_PROMOTION, CONVERTED
    }
}
