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
 * The context that is passed to a handler for a payload that arrives during the configuration phase of the connection.
 *
 * @param handler A reply handler that can be used to send a reply to the sender.
 * @param packetHandler The packet handler that can be used to immediately process other packets.
 * @param taskCompletedHandler The task completed handler that can be used to indicate that a configuration task has been completed.
 * @param workHandler A work handler that can be used to schedule work to be done on the main thread.
 * @param flow The flow of the packet.
 * @param channelHandlerContext The channel handler context.
 * @param sender The sender of the payload.
 * @implNote The {@link #sender()} will always be empty, because no player is available during the configuration phase.
 */
public record ConfigurationPayloadContext(
        IReplyHandler handler,
        IPacketHandler packetHandler,
        ITaskCompletedHandler taskCompletedHandler,
        ISynchronizedWorkHandler workHandler,
        PacketFlow flow,
        ChannelHandlerContext channelHandlerContext,
        Optional<Player> sender) implements IPayloadContext {
    @Override
    public ConnectionProtocol protocol() {
        return ConnectionProtocol.CONFIGURATION;
    }
}
