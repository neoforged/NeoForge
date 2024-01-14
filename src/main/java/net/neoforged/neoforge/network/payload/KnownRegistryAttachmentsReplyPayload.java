package net.neoforged.neoforge.network.payload;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;

public record KnownRegistryAttachmentsReplyPayload(
        Map<ResourceKey<Registry<?>>, Collection<ResourceLocation>> attachments) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:known_registry_attachments_reply");

    public KnownRegistryAttachmentsReplyPayload(FriendlyByteBuf buf) {
        this(buf.readMap(b1 -> (ResourceKey<Registry<?>>) (Object) b1.readRegistryKey(), b1 -> b1.readList(FriendlyByteBuf::readResourceLocation)));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(attachments, FriendlyByteBuf::writeResourceKey, (b1, list) -> b1.writeCollection(list, FriendlyByteBuf::writeResourceLocation));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
