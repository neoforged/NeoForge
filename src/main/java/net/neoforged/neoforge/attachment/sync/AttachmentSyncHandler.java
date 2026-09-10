/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

/// Manages how data attachments are written (on the server) and read (on the client) from packets.
///
/// Sync is handled automatically in the following cases:
///
///   - A client is receiving initial data for this attachment holder.
///   - An attachment is default-created through [IAttachmentHolder#getData(AttachmentType)].
///   - An attachment is updated through [IAttachmentHolder#setData(AttachmentType, Object)].
///   - An attachment is removed through [IAttachmentHolder#removeData(AttachmentType)].
///
/// For other cases such as modifications to mutable synced attachments,
/// [AttachmentHolderSyncHandler#syncData(AttachmentType)] can be called to trigger syncing.
public interface AttachmentSyncHandler<T> {
    /// Decides whether data should be sent to some player that can see the holder.
    ///
    /// By default, all players that can see the holder are sent the data.
    /// A typical use case for this method is to only send player-specific data to that player.
    ///
    /// The returned value should be consistent for a given holder and player.
    ///
    /// @param holder the holder for the attachment, can be cast if the subtype is known
    /// @param to     the player that might receive the data
    /// @return `true` to send data to the player, `false` otherwise
    default boolean sendToPlayer(IAttachmentHolder holder, ServerPlayer to) {
        return true;
    }

    /// Writes attachment data to a buffer.
    ///
    /// If `initialSync` is `true`,
    /// the data should be written in full because the client does not have any previous data.
    ///
    /// If `initialSync` is `false`,
    /// the client already received a previous version of the data.
    /// In this case, this method is only called once for the attachment,
    /// and the resulting data is broadcast to all relevant players.
    ///
    /// If nothing is written to the buffer, nothing is sent to the client at all,
    /// and [#read] will not be called on the client side.
    void write(RegistryFriendlyByteBuf buf, T attachment, boolean initialSync);

    /// Reads attachment data on the client side.
    ///
    /// @param holder        the attachment holder, can be cast if the subtype is known
    /// @param previousValue the previous value of the attachment, or `null` if there was no previous value
    /// @return the new value of the attachment, or `null` if the attachment should be removed
    @Nullable
    T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue);
}
