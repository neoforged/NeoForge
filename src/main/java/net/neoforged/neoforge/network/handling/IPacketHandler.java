/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Describes a replyHandler for a packet.
 * Allows for the handling of full packets from custom payloads
 */
public interface IPacketHandler {

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
     * Trigger a disconnect from the network.
     *
     * @param reason The reason for the disconnect.
     */
    void disconnect(Component reason);
}
