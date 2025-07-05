/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.network.event;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.network.registration.ClientNetworkRegistry;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired on the mod event bus when the {@link ClientNetworkRegistry} is being set up.
 * <p>
 * This event is used to assign payload handlers to clientbound payload types.
 */
public class RegisterClientPayloadHandlersEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public RegisterClientPayloadHandlersEvent() {}

    /**
     * Registers the provided {@link IPayloadHandler} as the client handler to be invoked on the main thread
     * for the provided {@link CustomPacketPayload.Type}
     *
     * @param type    The payload type to register the handler for
     * @param handler The client-side payload handler to register
     */
    public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, IPayloadHandler<T> handler) {
        this.register(type, HandlerThread.MAIN, handler);
    }

    /**
     * Registers the provided {@link IPayloadHandler} as the client handler to be invoked on the specified thread
     * for the provided {@link CustomPacketPayload.Type}
     *
     * @param type    The payload type to register the handler for
     * @param thread  The thread the handler should be invoked on
     * @param handler The client-side payload handler to register
     */
    public <T extends CustomPacketPayload> void register(CustomPacketPayload.Type<T> type, HandlerThread thread, IPayloadHandler<T> handler) {
        ClientNetworkRegistry.register(type, thread, handler);
    }
}
