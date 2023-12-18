/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;

import java.util.OptionalInt;

public interface IPayloadContext {
    IReplyHandler handler();
    
    IPacketHandler packetHandler();
    
    ISynchronizedWorkHandler workHandler();
    
    PacketFlow flow();
    
    ConnectionProtocol protocol();
    
    ChannelHandlerContext channelHandlerContext();
}
