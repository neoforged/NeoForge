package net.neoforged.neoforge.network.payload;

import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MinecraftUnregisterPayload(Set<ResourceLocation> forgottenChannels) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("unregister");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = MinecraftUnregisterPayload::new;

    public MinecraftUnregisterPayload(FriendlyByteBuf buf) {
        this(DinnerboneProtocolUtils.readChannels(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        DinnerboneProtocolUtils.writeChannels(buf, forgottenChannels);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
