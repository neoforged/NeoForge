/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask.Type;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.IServerConfigurationPacketListenerExtension;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.Nullable;

public record ServerPayloadContext(ServerCommonPacketListener listener, ResourceLocation payloadId) implements IPayloadContext {
    @Override
    public void reply(CustomPacketPayload payload) {
        listener.send(payload);
    }

    @Override
    public void disconnect(Component reason) {
        listener.disconnect(reason);
    }

    @Override
    public void handle(Packet<?> packet) {
        NetworkRegistry.handlePacketUnchecked(packet, listener);
    }

    @Override
    public void handle(CustomPacketPayload payload) {
        handle(new ServerboundCustomPayloadPacket(payload));
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
        if (listener instanceof IServerConfigurationPacketListenerExtension ext) {
            ext.finishCurrentTask(type);
        } else {
            throw new UnsupportedOperationException("Attempted to complete a configuration task outside of the configuration phase!");
        }
    }

    @Override
    public PacketFlow flow() {
        return PacketFlow.SERVERBOUND;
    }

    @Override
    public ConnectionProtocol protocol() {
        return listener.protocol();
    }

    @Override
    public ChannelHandlerContext channelHandlerContext() {
        return listener.getConnection().channel().pipeline().lastContext();
    }

    @Nullable
    @Override
    public Player sender() {
        return this.listener instanceof ServerPlayerConnection spc ? spc.getPlayer() : null;
    }
}
