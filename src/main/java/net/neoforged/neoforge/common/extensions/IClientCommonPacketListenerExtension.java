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
    ClientCommonPacketListener self();

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

    /**
     * {@return the minecraft instance}
     */
    Minecraft getMinecraft();
}
