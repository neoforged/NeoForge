/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;

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
     * For handlers running on the network thread, submits the given task to be run on the main thread of the game.
     * <p>
     * For handlers running on the main thread, immediately executes the task.
     * <p>
     * On the network thread, the future will be automatically guarded against exceptions using {@link CompletableFuture#exceptionally}.
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
     * {@return the current inbound protocol info of the connection}
     */
    ProtocolInfo<?> protocolInfo();

    /**
     * {@return the flow of the packet}
     */
    default PacketFlow flow() {
        return protocolInfo().flow();
    }

    /**
     * {@return the protocol of the connection}
     */
    default ConnectionProtocol protocol() {
        return protocolInfo().id();
    }

    /**
     * {@return the channel handler context}
     */
    ChannelHandlerContext channelHandlerContext();

    /**
     * Retrieves the player relevant to this payload, which has a different meaning based on the protocol and flow.
     * <p>
     * For server-bound play payloads, retrieves the sending player. This case can safely be cast to {@link ServerPlayer}.
     * <p>
     * For client-bound payloads, retrieves the client player.
     * 
     * @throws UnsupportedOperationException when called on the server during the configuration phase.
     */
    Player player();

    /**
     * {@return the connection}
     */
    Connection connection();
}
