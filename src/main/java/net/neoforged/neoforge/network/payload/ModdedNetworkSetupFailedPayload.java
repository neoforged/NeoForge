package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record ModdedNetworkSetupFailedPayload(Map<ResourceLocation, Component> failureReasons) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = new ResourceLocation("neoforge", "modded_network_setup_failed");
    public static final FriendlyByteBuf.Reader<? extends CustomPacketPayload> READER = ModdedNetworkSetupFailedPayload::new;
    
    public ModdedNetworkSetupFailedPayload(FriendlyByteBuf buf) {
        this(buf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readComponent));
    }
    
    @Override
    public void write(FriendlyByteBuf p_294947_) {
        p_294947_.writeMap(failureReasons, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeComponent);
    }
    
    @Override
    public ResourceLocation id() {
        return ID;
    }
}
