/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Defines a builder for a direction aware payload handler.
 *
 * @param <P> The type of payload.
 * @param <T> The type of handler.
 */
public interface IDirectionAwarePayloadHandlerBuilder<P extends CustomPacketPayload, T> {
    /**
     * Sets the client side handler.
     *
     * @param clientSide The client side handler.
     * @return This builder.
     */
    IDirectionAwarePayloadHandlerBuilder<P, T> client(T clientSide);

    /**
     * Sets the server side handler.
     *
     * @param serverSide The server side handler.
     * @return This builder.
     */
    IDirectionAwarePayloadHandlerBuilder<P, T> server(T serverSide);
}
