/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;

/**
 * Forge extension interface to deal with custom forge query payloads.
 */
public interface IForgeCustomQueryPayload extends CustomQueryPayload, CustomQueryAnswerPayload {

    /**
     * The buffer that created the query payload.
     * @return The buffer
     */
    FriendlyByteBuf buffer();

    /**
     * The internal packet processing index for the query.
     *
     * @return The packet index.
     */
    int packetIndex();

    /**
     * The network direction in which this payload can be transmitted.
     *
     * @param packet The packet to get the network direction for if this payload was transmitted
     * @return The network direction for this payload.
     */
    default LoginNetworkDirection getDirection(Packet<?> packet) {
        return LoginNetworkDirection.directionForPayload(packet.getClass());
    }
}
