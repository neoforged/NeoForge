package net.neoforged.neoforge.attachment;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface IAttachmentSyncHandler<T> {
    // TODO: pass target player
    void write(RegistryFriendlyByteBuf buf, T attachment, ServerPlayer to, AttachmentSyncReason reason);

    @Nullable
    T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf);
}
