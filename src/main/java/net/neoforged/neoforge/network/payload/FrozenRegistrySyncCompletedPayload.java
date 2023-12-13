package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FrozenRegistrySyncCompletedPayload() implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge:frozen_registry_sync_completed");
    
    @Override
    public void write(@NotNull FriendlyByteBuf p_294947_) {
    
    }
    
    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
