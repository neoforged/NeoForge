/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * This interface is used to extend the {@link ClientCommonPacketListener} interface.
 * <p>
 * Its primary purpose is to expose sending logic, for transmitting packets to the server.
 * </p>
 */
public interface IClientCommonPacketListenerExtension {
    /**
     * {@return the ClientCommonPacketListener this extension is attached to}
     */
    private ClientCommonPacketListener self() {
        return (ClientCommonPacketListener) this;
    }

    /**
     * Sends a packet to the server.
     * 
     * @param packet The packet to send.
     */
    void send(Packet<?> packet);

    /**
     * Sends a custom payload to the server.
     *
     * @param payload The payload to send.
     */
    default void send(CustomPacketPayload payload) {
        send(new ServerboundCustomPayloadPacket(payload));
    }

    /**
     * Exposes the raw underlying connection.
     *
     * @return The raw underlying connection.
     */
    Connection getConnection();

    /**
     * Exposes the raw underlying connection event loop that can be used to schedule tasks on the main thread.
     *
     * @return The raw underlying connection event loop.
     */
    default ReentrantBlockableEventLoop<?> getMainThreadEventLoop() {
        return getMinecraft();
    }

    /**
     * Checks if the connection has negotiated and opened a channel for the payload.
     *
     * @param payloadId The payload id to check
     * @returns true if a payload with this id may be sent over this connection.
     */
    default boolean hasChannel(final ResourceLocation payloadId) {
        return NetworkRegistry.hasChannel(self(), payloadId);
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
     * {@return the minecraft instance}
     */
    Minecraft getMinecraft();

    /**
     * {@return the connection type}
     */
    ConnectionType getConnectionType();
}
