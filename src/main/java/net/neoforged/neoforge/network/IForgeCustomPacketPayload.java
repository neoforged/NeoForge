/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Forge extension interface to deal with custom forge query payloads.
 */
public interface IForgeCustomPacketPayload extends CustomPacketPayload {

    /**
     * The buffer that created the query payload.
     * @return The buffer
     */
    FriendlyByteBuf buffer();

    /**
     * The index of the internal packet stored in the payload.
     *
     * @return The index of the internal packet
     */
    int packetIndex();

    /**
     * The network direction in which this payload can be transmitted.
     *
     * @param packet The packet for which to determine the network direction if this payload was transmitted.
     * @return The network direction for this payload.
     */
    default PlayNetworkDirection getDirection(Packet<?> packet) {
        return PlayNetworkDirection.directionForPayload(packet.getClass());
    }

}
