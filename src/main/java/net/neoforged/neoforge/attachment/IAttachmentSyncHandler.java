package net.neoforged.neoforge.attachment;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Manages how data attachments are written (on the server) and read (on the client) from packets.
 */
public interface IAttachmentSyncHandler<T> {
    // TODO: pass target player
    void write(RegistryFriendlyByteBuf buf, T attachment, ServerPlayer to, AttachmentSyncReason reason);

    // TODO: we could also return void and let the sync handler call .setData(type, xxx). But that means passing the type somehow.
    @Nullable
    T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf);
}
