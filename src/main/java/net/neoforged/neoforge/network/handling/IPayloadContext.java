/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.ApiStatus;

/**
 * Common context interface for payload handlers.
 */
@ApiStatus.NonExtendable
public interface IPayloadContext {
    /**
     * Retrieves the packet listener associated with this context.
     * <p>
     * For usability, this is typed to {@link ICommonPacketListener}, but can be downcast to the vanilla packet listeners if necessary.
     */
    ICommonPacketListener listener();

    /**
     * {@return the connection}
     */
    default Connection connection() {
        return this.listener().getConnection();
    }

    /**
     * Retrieves the player relevant to this payload. Players are only available in the {@link ConnectionProtocol#PLAY} phase.
     * <p>
     * For server-bound payloads, retrieves the sending {@link ServerPlayer}.
     * <p>
     * For client-bound payloads, retrieves the receiving {@link LocalPlayer}.
     * 
     * @throws UnsupportedOperationException when called on the server during the configuration phase.
     */
    Player player();

    /**
     * Sends the given payload back to the sender.
     *
     * @param payload The payload to send.
     */
    default void reply(CustomPacketPayload payload) {
        this.listener().send(payload);
    }

    /**
     * Disconnects the player from the network.
     */
    default void disconnect(Component reason) {
        this.listener().disconnect(reason);
    }

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
     * {@return the flow of the received payload}
     */
    PacketFlow flow();

    /**
     * {@return the protocol of the connection}
     */
    default ConnectionProtocol protocol() {
        return this.listener().protocol();
    }

    /**
     * Handles a packet using the current context.
     * <p>
     * Used to trigger vanilla handling when custom payloads may be transformed into a vanilla packet.
     *
     * @param packet The packet.
     */
    default void handle(Packet<?> packet) {
        NetworkRegistry.handlePacketUnchecked(packet, this.listener());
    }

    /**
     * Handles a payload using the current context.
     * <p>
     * Used to handle sub-payloads if necessary.
     *
     * @param payload The payload.
     */
    void handle(CustomPacketPayload payload);

    /**
     * Marks a {@link ConfigurationTask} as completed.
     *
     * @param type The type of task that was completed.
     * @throws UnsupportedOperationException if called on the client, or called on the server outside of the configuration phase.
     */
    void finishCurrentTask(ConfigurationTask.Type type);

    /**
     * {@return the channel handler context}
     */
    default ChannelHandlerContext channelHandlerContext() {
        return this.connection().channel().pipeline().lastContext();
    }
}
