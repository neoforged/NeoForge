package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;

import java.util.OptionalInt;

public record ConfigurationPayloadContext(
        IReplyHandler handler,
        IPacketHandler packetHandler,
        ITaskCompletedHandler taskCompletedHandler,
        ISynchronizedWorkHandler workHandler,
        PacketFlow flow,
        ChannelHandlerContext channelHandlerContext
) implements IPayloadContext {
    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.CONFIGURATION;
    }
}
