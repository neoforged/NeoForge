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
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.Nullable;

/**
 * Common context interface for payload handlers.
 */
public interface IPayloadContext {
    /**
     * Sends the given payload back to the sender.
     *
     * @param payload The payload to send.
     */
    void reply(CustomPacketPayload payload);

    /**
     * Disconnects the player from the network.
     */
    void disconnect(Component reason);

    /**
     * Handles a packet using the current context.
     * <p>
     * Used to trigger vanilla handling when custom payloads may be transformed into a vanilla packet.
     *
     * @param packet The packet.
     */
    void handle(Packet<?> packet);

    /**
     * Handles a payload using the current context.
     * <p>
     * Used to handle sub-payloads if necessary.
     *
     * @param payload The payload.
     */
    void handle(CustomPacketPayload payload);

    /**
     * Submits the given task to be run on the main thread of the game.
     * <p>
     * The returned future will be automatically guarded against exceptions using {@link CompletableFuture#exceptionally}.
     * If you need to catch your own exceptions, use a try/catch block within your task.
     * 
     * @param task The task to run.
     */
    CompletableFuture<Void> enqueueWork(Runnable task);

    /**
     * @see #enqueueWork(Runnable)
     */
    <T> CompletableFuture<T> enqueueWork(Supplier<T> task);

    /**
     * Marks a {@link ConfigurationTask} as completed.
     *
     * @param type The type of task that was completed.
     * @throws UnsupportedOperationException if called on the client, or called on the server outside of the configuration phase.
     */
    void finishCurrentTask(ConfigurationTask.Type type);

    /**
     * {@return the flow of the packet}
     */
    PacketFlow flow();

    /**
     * {@return the protocol of the connection}
     */
    ConnectionProtocol protocol();

    /**
     * {@return the channel handler context}
     */
    ChannelHandlerContext channelHandlerContext();

    /**
     * When available, gets the sender for packets that are sent from a client to the server.
     * <p>
     * A sending player is unavailable during the configuration phase.
     */
    @Nullable
    ServerPlayer sender();
}
