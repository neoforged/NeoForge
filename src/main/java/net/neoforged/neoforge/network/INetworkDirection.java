/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;

public interface INetworkDirection<TDirection extends INetworkDirection<TDirection>> {
    TDirection reply();

    LogicalSide getOriginationSide();

    LogicalSide getReceptionSide();

    Packet<?> buildPacket(PacketData packetData, ResourceLocation channelName);

    record PacketData(FriendlyByteBuf buffer, int index) {}
}
