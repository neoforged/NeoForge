/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.persistence.AttachmentHolderPersistenceHandler;
import net.neoforged.neoforge.attachment.storage.AttachmentDataReader;
import net.neoforged.neoforge.attachment.storage.AttachmentDataWriter;
import net.neoforged.neoforge.attachment.sync.AttachmentHolderSyncHandler;
import net.neoforged.neoforge.attachment.sync.AttachmentSyncHandler;
import org.jspecify.annotations.Nullable;

/**
 * An object that can hold data attachments.
 */
public interface IAttachmentHolder extends AttachmentDataReader, AttachmentDataWriter {
    @Nullable
    default AttachmentHolderSyncHandler attachmentSyncHandler() {
        return null;
    }

    @Nullable
    default AttachmentHolderPersistenceHandler attachmentPersistenceHandler() {
        return null;
    }

    /**
     * Syncs a data attachment of the given type with all relevant clients.
     *
     * <p>If there is currently no attachment of the given type,
     * the removal of the attachment is synced to the client.
     *
     * @see AttachmentSyncHandler
     * @see AttachmentHolderSyncHandler
     * @deprecated Use {@link #attachmentSyncHandler()} for sync
     */
    @Deprecated(forRemoval = true)
    default void syncData(AttachmentType<?> type) {
        var sync = attachmentSyncHandler();
        if (sync != null)
            sync.syncData(type);
    }

    /**
     * Syncs a data attachment of the given type with all relevant clients.
     *
     * <p>If there is currently no attachment of the given type,
     * the removal of the attachment is synced to the client.
     *
     * @see AttachmentSyncHandler
     * @see AttachmentHolderSyncHandler
     * @deprecated Use {@link #attachmentSyncHandler()} for sync
     */
    @Deprecated(forRemoval = true)
    default void syncData(Supplier<? extends AttachmentType<?>> type) {
        syncData(type.get());
    }
}
