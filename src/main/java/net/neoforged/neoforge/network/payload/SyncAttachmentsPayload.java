/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.attachment.AttachmentSync;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record SyncAttachmentsPayload(
        Target target,
        List<AttachmentType<?>> types,
        byte[] syncPayload)
        implements CustomPacketPayload {
    public static final Type<SyncAttachmentsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "sync_attachments"));;
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAttachmentsPayload> STREAM_CODEC = StreamCodec.composite(
            Target.STREAM_CODEC,
            SyncAttachmentsPayload::target,
            ByteBufCodecs.registry(AttachmentSync.SYNCED_ATTACHMENT_TYPES.key()).apply(ByteBufCodecs.list()),
            SyncAttachmentsPayload::types,
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            SyncAttachmentsPayload::syncPayload,
            SyncAttachmentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public sealed interface Target {
        StreamCodec<RegistryFriendlyByteBuf, Target> STREAM_CODEC = StreamCodec.of(
                (buf, target) -> {
                    switch (target) {
                        case BlockEntityTarget(var pos) -> {
                            buf.writeByte(0);
                            buf.writeBlockPos(pos);
                        }
                        case ChunkTarget(var pos) -> {
                            buf.writeByte(1);
                            buf.writeChunkPos(pos);
                        }
                        case EntityTarget(var entityId) -> {
                            buf.writeByte(2);
                            buf.writeVarInt(entityId);
                        }
                        case LevelTarget() -> {
                            buf.writeByte(3);
                        }
                    }
                },
                buf -> {
                    int type = buf.readByte();
                    switch (type) {
                        case 0 -> {
                            return new BlockEntityTarget(buf.readBlockPos());
                        }
                        case 1 -> {
                            return new ChunkTarget(buf.readChunkPos());
                        }
                        case 2 -> {
                            return new EntityTarget(buf.readVarInt());
                        }
                        case 3 -> {
                            return new LevelTarget();
                        }
                        default -> throw new IllegalArgumentException("Unknown target type: " + type);
                    }
                });
    }

    public record BlockEntityTarget(BlockPos pos) implements Target {}

    public record ChunkTarget(ChunkPos pos) implements Target {}

    public record EntityTarget(int entity) implements Target {}

    public record LevelTarget() implements Target {}
}
