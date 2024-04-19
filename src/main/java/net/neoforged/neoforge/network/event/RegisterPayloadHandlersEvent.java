/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.event;

import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when the {@link NetworkRegistry} is being set up.
 * <p>
 * This event is used to collect all the payload types and their handlers that should be used on the network.
 * <p>
 * There are a few base rules for custom payloads:
 * <ul>
 * <li>Payloads should not write their ids within their {@link StreamCodec}, as this will be done automatically.</li>
 * <li>Payloads must be registered before they can be sent over the network.</li>
 * <li>Payloads may only be sent over the {@link ConnectionProtocol} they were registered for.</li>
 * <li>By default, {@link PayloadRegistrar} will wrap handlers so they execute on the main thread instead of the network thread.</li>
 * <li>Payloads are always serialized, including for {@link Connection#isMemoryConnection() memory connections} (singleplayer and LAN hosts).</li>
 * <ul>
 */
public class RegisterPayloadHandlersEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public RegisterPayloadHandlersEvent() {}

    /**
     * Creates a new {@link PayloadRegistrar}, a utility for registering payloads using a builder-style format.
     * 
     * @param version The network version. May not be empty
     */
    public PayloadRegistrar registrar(String version) {
        return new PayloadRegistrar(version);
    }

    public static enum HandlerThread {
        /**
         * The main thread of the receiving side.
         * <p>
         * On the logical client, this is the Render Thread.
         * <p>
         * On the logical server, this is the Server Thread.
         */
        MAIN,

        /**
         * The network thread, which executes concurrently to the main thread.
         */
        NETWORK;
    }
}
