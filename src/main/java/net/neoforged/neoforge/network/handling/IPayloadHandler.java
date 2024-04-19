/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent.HandlerThread;
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
     * When using {@link PayloadRegistrar} to register payloads, the executing thread defaults to the main thread. See {@link PayloadRegistrar#executesOn}.
     * <p>
     * When using {@link RegisterPayloadHandlersEvent#register}, the executing thread is always {@link HandlerThread#NETWORK}.
     */
    void handle(T payload, IPayloadContext context);
}
