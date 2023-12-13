package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.ForgeRegistry;

public record FrozenRegistryPayload(ResourceLocation registryName, ForgeRegistry.Snapshot snapshot) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge:frozen_registry");
    
    public FrozenRegistryPayload(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), ForgeRegistry.Snapshot.read(buf));
    }
    
    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(registryName);
        snapshot.write(buf);
    }
    
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
