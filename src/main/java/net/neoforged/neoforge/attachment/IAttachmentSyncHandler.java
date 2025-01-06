package net.neoforged.neoforge.attachment;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Manages how data attachments are written (on the server) and read (on the client) from packets.
 *
 * <p>Sync is handled automatically in the following cases:
 * <ul>
 *     <li>A client is receiving initial data for this attachment holder.</li>
 *     <li>An attachment is default-created through {@link IAttachmentHolder#getData(AttachmentType)}.</li>
 *     <li>An attachment is updated through {@link IAttachmentHolder#setData(AttachmentType, Object)}.</li>
 *     <li>An attachment is removed through {@link IAttachmentHolder#removeData(AttachmentType)}.</li>
 * </ul>
 *
 * <p>For other cases such as modifications to mutable synced attachments,
 * {@link IAttachmentHolder#syncData(AttachmentType)} can be called to trigger syncing.
 */
public interface IAttachmentSyncHandler<T> {
    default boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }

    void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync);

    // TODO: we could also return void and let the sync handler call .setData(type, xxx). But that means passing the type somehow.
    @Nullable
    T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf);
}
