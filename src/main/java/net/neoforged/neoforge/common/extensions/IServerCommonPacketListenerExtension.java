/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import javax.annotation.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * Extension class for {@link net.minecraft.network.protocol.common.ServerCommonPacketListener}
 * <p>
 * This interface and its default methods is used to make sending custom payloads easier.
 * </p>
 */
public interface IServerCommonPacketListenerExtension {
    /**
     * {@return the {@link ServerCommonPacketListener} that the extensions is attached to}
     */
    private ServerCommonPacketListener self() {
        return (ServerCommonPacketListener) this;
    }

    /**
     * Sends a packet to the client which this listener is attached to.
     *
     * @param packet The packet to send
     */
    void send(Packet<?> packet);

    /**
     * Sends a custom payload to the client which this listener is attached to.
     *
     * @param packetPayload The payload to send
     */
    default void send(CustomPacketPayload packetPayload) {
        this.send(new ClientboundCustomPayloadPacket(packetPayload));
    }

    /**
     * Sends a packet to the client which this listener is attached to.
     *
     * @param packet             The packet to send
     * @param packetSendListener The listener to call when the packet is sent
     */
    void send(Packet<?> packet, @Nullable PacketSendListener packetSendListener);

    /**
     * Sends a custom payload to the client which this listener is attached to.
     *
     * @param packetPayload The payload to send
     * @param listener      The listener to call when the packet is sent
     */
    default void send(CustomPacketPayload packetPayload, @Nullable PacketSendListener listener) {
        this.send(new ClientboundCustomPayloadPacket(packetPayload), listener);
    }

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
     * {@return the connection type of the connection}
     */
    ConnectionType getConnectionType();
}
