/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Callback for handling custom packets.
 *
 * @param <T> The type of payload.
 */
@FunctionalInterface
public interface IConfigurationPayloadHandler<T extends CustomPacketPayload> {

    /**
     * Invoked to handle the given payload in the given context.
     *
     * @param payload The payload.
     * @param context The context.
     */
    void handle(T payload, ConfigurationPayloadContext context);

    /**
     * Creates a handler that does nothing.
     *
     * @return The handler.
     * @param <Z> The type of payload.
     */
    static <Z extends CustomPacketPayload> IConfigurationPayloadHandler<Z> noop() {
        return (payload, context) -> {};
    }
}
