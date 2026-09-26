/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.persistence.AttachmentHolderPersistenceHandler;
import net.neoforged.neoforge.attachment.persistence.AttachmentPersistence;
import net.neoforged.neoforge.attachment.persistence.ValueIOPersistenceHandler;
import net.neoforged.neoforge.attachment.storage.AttachmentDataStorage;
import net.neoforged.neoforge.attachment.storage.SynchronizingAttachmentHolderDataStorage;
import net.neoforged.neoforge.attachment.sync.AttachmentHolderSyncHandler;

/**
 * Implementation class for objects that can hold data attachments.
 * For the user-facing methods, see {@link IAttachmentHolder}.
 */
public class AttachmentHolder implements AttachmentDataAccessor, IAttachmentHolder {
    @Deprecated(forRemoval = true)
    public static final String ATTACHMENTS_NBT_KEY = AttachmentPersistence.ATTACHMENTS_NBT_KEY;

    private final AttachmentHolderSyncHandler syncHandler = new AttachmentHolderSyncHandler() {
        @Override
        public void syncData(AttachmentType<?> type) {}

        @Override
        public void receiveData(RegistryAccess registryAccess, List<AttachmentType<?>> types, byte[] bytes) {}
    };

    private final SynchronizingAttachmentHolderDataStorage dataStorage = new SynchronizingAttachmentHolderDataStorage(this, syncHandler);

    private final AttachmentHolderPersistenceHandler persistenceHandler = new ValueIOPersistenceHandler(this, dataStorage);

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
        return dataStorage;
    }

    /**
     * Writes the serializable attachments to a tag.
     */
    public final void serializeAttachments(ValueOutput tag) {
        persistenceHandler.serialize(tag);
    }

    /**
     * Reads serializable attachments from a tag previously created via {@link #serializeAttachments(ValueOutput)}.
     *
     * <p>This does not trigger {@link AttachmentHolderSyncHandler#syncData syncing} of the deserialized attachments.
     */
    protected final void deserializeAttachments(ValueInput input) {
        persistenceHandler.deserialize(input);
    }
}
