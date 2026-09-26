/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.capability;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.persistence.AttachmentHolderPersistenceHandler;
import net.neoforged.neoforge.attachment.storage.AttachmentDataStorage;
import net.neoforged.neoforge.attachment.sync.AttachmentHolderSyncHandler;

public class AttachmentCapabilities {
    public static final AttachmentCapability<IAttachmentHolder, Void> ATTACHMENT_HOLDER = AttachmentCapability.createVoid(id("attachment_holder"), IAttachmentHolder.class);

    public static final AttachmentCapability<AttachmentDataStorage, Void> DATA_STORAGE = AttachmentCapability.createVoid(id("data_storage"), AttachmentDataStorage.class);

    public static final AttachmentCapability<AttachmentHolderPersistenceHandler, Void> PERSISTENCE = AttachmentCapability.createVoid(id("data_persistence"), AttachmentHolderPersistenceHandler.class);

    public static final AttachmentCapability<AttachmentHolderSyncHandler, Void> SYNC = AttachmentCapability.createVoid(id("data_sync"), AttachmentHolderSyncHandler.class);

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("neoforge", path);
    }
}
