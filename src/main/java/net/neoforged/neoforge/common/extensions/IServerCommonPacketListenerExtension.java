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
    ServerCommonPacketListener self();

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
    void send(CustomPacketPayload packetPayload);

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
     * {@return true if the connection is to a vanilla client}
     */
    default boolean isVanillaConnection() {
        return NetworkRegistry.getInstance().isVanillaConnection(getConnection());
    }

    /**
     * {@return true if the custom payload type with the given id is usable by this connection}
     *
     * @param payloadId The payload id to check
     */
    default boolean isConnected(final ResourceLocation payloadId) {
        return NetworkRegistry.getInstance().isConnected(self(), payloadId);
    }

    /**
     * {@return true if the custom payload is usable by this connection}
     * 
     * @param payload The payload to check
     */
    default boolean isConnected(final CustomPacketPayload payload) {
        return isConnected(payload.id());
    }
}
