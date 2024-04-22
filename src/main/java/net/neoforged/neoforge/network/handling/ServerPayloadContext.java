/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask.Type;
import net.minecraft.server.network.ServerPlayerConnection;
import net.neoforged.neoforge.common.extensions.IServerConfigurationPacketListenerExtension;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record ServerPayloadContext(ServerCommonPacketListener listener, ResourceLocation payloadId) implements IPayloadContext {
    @Override
    public void handle(CustomPacketPayload payload) {
        handle(new ServerboundCustomPayloadPacket(payload));
    }

    @Override
    public CompletableFuture<Void> enqueueWork(Runnable task) {
        if (listener.getMainThreadEventLoop().isSameThread()) {
            task.run();
            return CompletableFuture.completedFuture(null);
        }
        return NetworkRegistry.guard(listener.getMainThreadEventLoop().submit(task), this.payloadId);
    }

    @Override
    public <T> CompletableFuture<T> enqueueWork(Supplier<T> task) {
        if (listener.getMainThreadEventLoop().isSameThread()) {
            return CompletableFuture.completedFuture(task.get());
        }
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
    public ServerPlayer player() {
        if (this.listener instanceof ServerPlayerConnection spc) {
            return spc.getPlayer();
        }
        throw new UnsupportedOperationException("Cannot retrieve a sending player during the configuration phase.");
    }
}
