package net.neoforged.neoforge.network.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public record SyncEntityAttachmentsPayload(
        int entity,
        List<AttachmentType<?>> types,
        byte[] syncPayload)
        implements CustomPacketPayload {
    public static final Type<SyncEntityAttachmentsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "sync_entity_attachments"));;
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEntityAttachmentsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncEntityAttachmentsPayload::entity,
            ByteBufCodecs.registry(NeoForgeRegistries.Keys.ATTACHMENT_TYPES).apply(ByteBufCodecs.list()),
            SyncEntityAttachmentsPayload::types,
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            SyncEntityAttachmentsPayload::syncPayload,
            SyncEntityAttachmentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
