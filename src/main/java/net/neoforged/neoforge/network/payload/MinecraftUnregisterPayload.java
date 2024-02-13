package net.neoforged.neoforge.network.payload;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecraftUnregisterPayload(Set<ResourceLocation> forgottenChannels) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("unregister");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = MinecraftUnregisterPayload::new;

    public MinecraftUnregisterPayload(FriendlyByteBuf buf) {
        this(buf.<ResourceLocation, Set<ResourceLocation>>readSplit(HashSet::new, ResourceLocation::new, "\0"));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeSplit(forgottenChannels, ResourceLocation::toString, "\0");
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
