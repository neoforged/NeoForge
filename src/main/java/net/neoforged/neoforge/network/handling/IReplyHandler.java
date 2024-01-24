/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Interface for handling replies on custom packets.
 */
@FunctionalInterface
public interface IReplyHandler {
    /**
     * Sends the given payload back to the player.
     *
     * @param payload The payload to send back.
     */
    void send(CustomPacketPayload payload);
}
