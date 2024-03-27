/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a builder for a direction aware payload handler.
 *
 * @param <T> The type of payload.
 */
public final class DirectionalPayloadHandlerBuilder<T extends CustomPacketPayload> {
    private @Nullable IPayloadHandler<T> clientSide;
    private @Nullable IPayloadHandler<T> serverSide;

    /**
     * Sets the client side handler.
     *
     * @param clientSide The client side handler.
     * @return this.
     */
    public DirectionalPayloadHandlerBuilder<T> client(IPayloadHandler<T> clientSide) {
        this.clientSide = clientSide;
        return this;
    }

    /**
     * Sets the server side handler.
     *
     * @param serverSide The server side handler.
     * @return this.
     */
    public DirectionalPayloadHandlerBuilder<T> server(IPayloadHandler<T> serverSide) {
        this.serverSide = serverSide;
        return this;
    }

    DirectionalPayloadHandler<T> build() {
        return new DirectionalPayloadHandler<T>(clientSide, serverSide);
    }
}
