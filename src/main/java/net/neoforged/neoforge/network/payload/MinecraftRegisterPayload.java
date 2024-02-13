package net.neoforged.neoforge.network.payload;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecraftRegisterPayload(Set<ResourceLocation> newChannels) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("register");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = MinecraftRegisterPayload::new;

    public MinecraftRegisterPayload(FriendlyByteBuf buf) {
        this(buf.<ResourceLocation, Set<ResourceLocation>>readSplit(HashSet::new, ResourceLocation::new, "\0"));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeSplit(newChannels, ResourceLocation::toString, "\0");
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
