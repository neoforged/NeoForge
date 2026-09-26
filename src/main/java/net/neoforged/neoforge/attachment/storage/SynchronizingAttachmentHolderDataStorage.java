/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.storage;

import java.util.Map;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.sync.AttachmentHolderSyncHandler;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

/// A data storage implementation that also sends sync update notifications to a sync handler.
/// This recreates the old functionality of [AttachmentHolder] where everything was in one place.
public class SynchronizingAttachmentHolderDataStorage extends SimpleAttachmentDataStorage {
    private final AttachmentHolderSyncHandler syncHandler;

    public SynchronizingAttachmentHolderDataStorage(IAttachmentHolder holder, AttachmentHolderSyncHandler syncHandler) {
        super(holder);
        this.syncHandler = syncHandler;
    }

    /**
     * Create the attachment map if it does not yet exist, or return the current map.
     */
    public final Map<AttachmentType<?>, Object> getAttachmentMap() {
        return attachments;
    }

    @Override
    @MustBeInvokedByOverriders
    public <T> @Nullable T removeData(AttachmentType<T> type) {
        T old = super.removeData(type);
        syncHandler.syncData(type);
        return old;
    }

    @Override
    @MustBeInvokedByOverriders
    public @Nullable <T> T setData(AttachmentType<T> type, T data) {
        var d = super.setData(type, data);
        syncHandler.syncData(type);
        return d;
    }
}
