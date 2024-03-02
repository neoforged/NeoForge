package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask.Type;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public record ClientPayloadContext(ClientCommonPacketListener listener, ResourceLocation payloadId) implements IPayloadContext {

    @Override
    public void reply(CustomPacketPayload payload) {
        listener.send(payload);
    }

    @Override
    public void disconnect(Component reason) {
        listener.getConnection().disconnect(reason);
    }

    @Override
    public void handle(Packet<?> packet) {
        NetworkRegistry.handlePacketUnchecked(packet, listener);
    }

    @Override
    public void handle(CustomPacketPayload payload) {
        handle(new ClientboundCustomPayloadPacket(payload));
    }

    @Override
    public CompletableFuture<Void> enqueueWork(Runnable task) {
        return NetworkRegistry.guard(listener.getMainThreadEventLoop().submit(task), this.payloadId);
    }

    @Override
    public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        return NetworkRegistry.guard(listener.getMainThreadEventLoop().submit(task), this.payloadId);
    }

    @Override
    public void finishCurrentTask(Type type) {
        throw new UnsupportedOperationException("Attempted to complete a configuration task on the client!");
    }

    @Override
    public PacketFlow flow() {
        return PacketFlow.CLIENTBOUND;
    }

    @Override
    public ConnectionProtocol protocol() {
        return listener.protocol();
    }

    @Nullable
    @Override
    @SuppressWarnings("resource")
    public Player sender() {
        return listener.getMinecraft().player;
    }

    @Override
    public ChannelHandlerContext channelHandlerContext() {
        return listener.getConnection().channel().pipeline().lastContext();
    }

}
