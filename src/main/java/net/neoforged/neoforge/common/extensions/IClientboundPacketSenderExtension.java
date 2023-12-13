package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

import javax.annotation.Nullable;

public interface IClientboundPacketSenderExtension {
    
    void send(CustomPacketPayload packetPayload);
    
    void send(CustomPacketPayload packetPayload, @Nullable PacketSendListener listener);
    
    void disconnect(Component p_294116_);
    
    Connection getConnection();
    
    ReentrantBlockableEventLoop<?> getMainThreadEventLoop();
}
