package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FrozenRegistrySyncCompletePayload() implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge:frozen_registry_sync_complete");
    
    public FrozenRegistrySyncCompletePayload(FriendlyByteBuf buf) {
        this();
    }
    
    @Override
    public void write(@NotNull FriendlyByteBuf buf) {
    }
    
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
