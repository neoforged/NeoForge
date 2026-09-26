/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.sync;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.attachment.AttachmentType;

/// Handles sync tasks for an instance of
public interface AttachmentHolderSyncHandler {
    /// Syncs a data attachment of the given type with all relevant clients.
    ///
    /// If there is currently no attachment of the given type,
    /// the removal of the attachment is synced to the client.
    ///
    /// @see AttachmentSyncHandler
    default void syncData(AttachmentType<?> type) {
        // Do nothing by default, implementers should override this method if needed.
    }

    /// Syncs a data attachment of the given type with all relevant clients.
    ///
    /// If there is currently no attachment of the given type,
    /// the removal of the attachment is synced to the client.
    ///
    /// @see AttachmentSyncHandler
    default void syncData(Supplier<? extends AttachmentType<?>> type) {
        syncData(type.get());
    }

    void receiveData(RegistryAccess registryAccess, List<AttachmentType<?>> types, byte[] bytes);
}
