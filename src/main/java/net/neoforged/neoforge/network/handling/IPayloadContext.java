/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Defines a phase-less payload context that is passed to a handler for a payload that arrives during the connection.
 */
public interface IPayloadContext {
    /**
     * {@return a handler that can be used to reply to the payload.}
     */
    IReplyHandler handler();
    
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
     * {@return the sender of the payload.}
     * @implNote This {@link Optional} will be empty if the payload was sent by the server, it will also be empty during the configuration phase.
     */
    Optional<Player> sender();
}
