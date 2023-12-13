package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ConfigFilePayload(byte[] contents, String fileName) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge", "config_file");
    
    public ConfigFilePayload(FriendlyByteBuf buf) {
        this(buf.readByteArray(), buf.readUtf());
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBytes(contents);
        buf.writeUtf(fileName);
    }
    
    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
