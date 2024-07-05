/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * Extension interface and functionality hoist for both {@link ServerCommonPacketListener}
 * and {@link ClientCommonPacketListener}.
 */
public interface ICommonPacketListener extends PacketListener {
    /**
     * Sends a packet to the target of this listener.
     */
    void send(Packet<?> packet);

    /**
     * Sends a payload to the target of this listener.
     */
    void send(CustomPacketPayload payload);

    /**
     * Triggers a disconnection with the given reason.
     *
     * @param reason The reason for the disconnection
     */
    void disconnect(Component reason);

    /**
     * {@return the connection this listener is attached to}
     */
    Connection getConnection();

    /**
     * {@return the main thread event loop}
     */
    ReentrantBlockableEventLoop<?> getMainThreadEventLoop();

    /**
     * Checks if the connection has negotiated and opened a channel for the payload.
     *
     * @param payloadId The payload id to check
     * @returns true if a payload with this id may be sent over this connection.
     */
    default boolean hasChannel(final ResourceLocation payloadId) {
        return NetworkRegistry.hasChannel(this.getConnection(), this.protocol(), payloadId);
    }

    /**
     * @see {@link #hasChannel(ResourceLocation)}
     */
    default boolean hasChannel(final CustomPacketPayload.Type<?> type) {
        return hasChannel(type.id());
    }

    /**
     * @see {@link #hasChannel(ResourceLocation)}
     */
    default boolean hasChannel(final CustomPacketPayload payload) {
        return hasChannel(payload.type());
    }

    /**
     * {@return the connection type of this packet listener}
     */
    ConnectionType getConnectionType();
}
