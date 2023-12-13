package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record FrozenRegistrySyncStartPayload(Set<ResourceLocation> toAccess) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge:frozen_registry_sync_start");
    
    public FrozenRegistrySyncStartPayload(FriendlyByteBuf buf) {
        this(buf.readSet(FriendlyByteBuf::readResourceLocation));
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeSet(toAccess, FriendlyByteBuf::writeResourceLocation);
    }
    
    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
