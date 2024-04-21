/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Callback for handling custom packets.
 *
 * @param <T> The type of payload.
 */
@FunctionalInterface
public interface IPayloadHandler<T extends CustomPacketPayload> {
    /**
     * Handles the payload with the supplied context.
     * <p>
     * The thread the supplied handler executes in depends on the {@link HandlerThread} set in {@link PayloadRegistrar#executesOn}.
     */
    void handle(T payload, IPayloadContext context);
}
