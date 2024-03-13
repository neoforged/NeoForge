/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import java.util.Optional;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.world.entity.player.Player;

/**
 * The context that is passed to a replyHandler for a payload that arrives during the configuration phase of the connection.
 *
 * @param replyHandler          A reply replyHandler that can be used to send a reply to the player.
 * @param packetHandler         The packet replyHandler that can be used to immediately process other packets.
 * @param workHandler           A work replyHandler that can be used to schedule work to be done on the main thread.
 * @param protocolInfo          The current protocol info of the connection.
 * @param channelHandlerContext The channel replyHandler context.
 * @param player                The player of the payload.
 * @implNote The {@link #player()} will be filled with the current client side player if the payload was sent by the server, the server will only populate this field if it is not configuring the client.
 */
public record PlayPayloadContext(
        IReplyHandler replyHandler,
        IPacketHandler packetHandler,
        ISynchronizedWorkHandler workHandler,
        ProtocolInfo<?> protocolInfo,
        ChannelHandlerContext channelHandlerContext,
        Optional<Player> player) implements IPayloadContext {}
