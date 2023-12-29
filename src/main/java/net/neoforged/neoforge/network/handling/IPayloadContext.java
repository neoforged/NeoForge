/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import java.util.Optional;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Defines a phase-less payload context that is passed to a handler for a payload that arrives during the connection.
 */
public interface IPayloadContext {
    /**
     * {@return a handler that can be used to reply to the payload.}
     */
    IReplyHandler replyHandler();

    /**
     * {@return a handler that can be used to have the current listener which received the payload handle another packet immediately.}
     */
    IPacketHandler packetHandler();

    /**
     * {@return a handler that can execute tasks on the main thread.}
     */
    ISynchronizedWorkHandler workHandler();

    /**
     * {@return the flow of the packet.}
     */
    PacketFlow flow();

    /**
     * {@return the protocol of the connection.}
     */
    ConnectionProtocol protocol();

    /**
     * {@return the channel handler context.}
     */
    ChannelHandlerContext channelHandlerContext();

    /**
     * {@return the player that acts within this context.}
     * 
     * @implNote This {@link Optional} will be filled with the current client side player if the payload was sent by the server, the server will only populate this field if it is not configuring the client.
     */
    Optional<Player> player();

    /**
     * {@return the level acts within this context.}
     *
     * @return This {@link Optional} will be filled with the current client side level if the payload was sent by the server, the server will only populate this field if it is not configuring the client.
     */
    default Optional<Level> level() {
        return player().map(Entity::level);
    }
}
