/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.entity.player.Player;

/**
 * Defines a phase-less payload context that is passed to a handler for a payload that arrives during the connection.
 */
public interface IPayloadContext {

    /**
     * Sends the given payload back to the player.
     *
     * @param payload The payload to send back.
     */
    void reply(CustomPacketPayload payload);

    /**
     * Disconnects the player from the network.
     */
    void disconnect(Component reason);
    
    /**
     * Invoked to handle the given packet.
     *
     * @param packet The packet.
     */
    void handle(Packet<?> packet);

    /**
     * Invoked to handle the given custom payload.
     *
     * @param payload The payload.
     */
    void handle(CustomPacketPayload payload);

    /**
     * Submits the given work to be run synchronously on the main thread of the game.
     * <p>
     * This method will <bold>not</bold> be guarded against exceptions.
     * <br>
     * If you need to guard against exceptions, call {@link CompletableFuture#exceptionally(Function)},
     * {@link CompletableFuture#exceptionallyAsync(Function)}}, or derivatives on the returned future.
     * </p>
     * 
     * @param task The task to run.
     */
    CompletableFuture<Void> enqueueWork(Runnable task);

    /**
     * @see #enqueueWork(Runnable)
     */
    <T> CompletableFuture<T> enqueueWork(Supplier<T> task);

    /**
     * Called when a task is completed.
     *
     * @param type The type of task that was completed.
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
     * {@return the player that acts within this context}
     * 
     * @implNote This {@link Optional} will be filled with the current client side player if the payload was sent by the server, the server will only populate this field if it is not configuring the client.
     */
    @Nullable
    Player sender();

}
