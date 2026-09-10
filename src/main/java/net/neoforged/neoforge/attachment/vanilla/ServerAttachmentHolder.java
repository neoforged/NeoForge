/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.vanilla;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.attachment.AttachmentDataAccessor;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.persistence.AttachmentHolderPersistenceHandler;
import net.neoforged.neoforge.attachment.persistence.impl.MinecraftServerSavedDataPersistenceHandler;
import net.neoforged.neoforge.attachment.storage.AttachmentDataStorage;
import net.neoforged.neoforge.attachment.storage.SimpleAttachmentDataStorage;
import net.neoforged.neoforge.attachment.sync.AttachmentHolderSyncHandler;

public class ServerAttachmentHolder implements IAttachmentHolder, AttachmentDataAccessor {
    private final AttachmentDataStorage storage;
    private final AttachmentHolderPersistenceHandler persistenceHandler;
    private final AttachmentHolderSyncHandler syncHandler = new AttachmentHolderSyncHandler() {
        @Override
        public void syncData(AttachmentType<?> type) {
            // TODO
        }

        @Override
        public void receiveData(RegistryAccess registryAccess, List<AttachmentType<?>> types, byte[] bytes) {}
    };

    public ServerAttachmentHolder(MinecraftServer server) {
        this.storage = new SimpleAttachmentDataStorage(this);
        this.persistenceHandler = new MinecraftServerSavedDataPersistenceHandler(server);
    }

    @Override
    public AttachmentHolderPersistenceHandler attachmentPersistenceHandler() {
        return persistenceHandler;
    }

    @Override
    public AttachmentHolderSyncHandler attachmentSyncHandler() {
        return syncHandler;
    }

    @Override
    public AttachmentDataStorage attachmentDataStorage() {
        return storage;
    }
}
