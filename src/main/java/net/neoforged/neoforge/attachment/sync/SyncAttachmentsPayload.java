/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment.sync;

import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record SyncAttachmentsPayload(
        ResourceKey<AttachmentHolderSyncHandler> syncHandlerKey,
        List<AttachmentType<?>> types,
        byte[] syncPayload)
        implements CustomPacketPayload {
    public static final Type<SyncAttachmentsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "sync_attachments"));;
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAttachmentsPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(NeoForgeRegistries.ATTACHMENT_HOLDER_SYNC_HANDLERS.key()),
            SyncAttachmentsPayload::syncHandlerKey,
            ByteBufCodecs.registry(AttachmentSync.SYNCED_ATTACHMENT_TYPES.key()).apply(ByteBufCodecs.list()),
            SyncAttachmentsPayload::types,
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            SyncAttachmentsPayload::syncPayload,
            SyncAttachmentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
