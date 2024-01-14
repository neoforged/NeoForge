package net.neoforged.neoforge.network.payload;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record KnownRegistryAttachmentsPayload(Map<ResourceKey<Registry<?>>, List<KnownAttachment>> attachments) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:known_registry_attachments");

    public KnownRegistryAttachmentsPayload(FriendlyByteBuf buf) {
        this(buf.readMap(b1 -> (ResourceKey<Registry<?>>) (Object) b1.readRegistryKey(), b1 -> b1.readList(b2 ->
                new KnownAttachment(b2.readResourceLocation(), b2.readBoolean()))));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(attachments, FriendlyByteBuf::writeResourceKey, (b1, list) -> b1.writeCollection(list,
                (b2, known) -> {
                    b2.writeResourceLocation(known.id());
                    b2.writeBoolean(known.mandatory());
                }));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public record KnownAttachment(ResourceLocation id, boolean mandatory) {}
}
