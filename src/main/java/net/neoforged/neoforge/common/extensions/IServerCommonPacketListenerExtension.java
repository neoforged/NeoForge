/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import javax.annotation.Nullable;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Extension interface for {@link ServerCommonPacketListener}
 */
public interface IServerCommonPacketListenerExtension extends ICommonPacketListener {
    /**
     * {@inheritDoc}
     */
    @Override
    default void send(CustomPacketPayload payload) {
        this.send(new ClientboundCustomPayloadPacket(payload));
    }

    /**
     * Sends a packet to the client of this listener.
     *
     * @param listener An optional callback for when the payload is sent
     */
    void send(Packet<?> packet, @Nullable PacketSendListener listener);

    /**
     * Sends a payload to the client of this listener.
     *
     * @param listener An optional callback for when the payload is sent
     */
    default void send(CustomPacketPayload payload, @Nullable PacketSendListener listener) {
        this.send(new ClientboundCustomPayloadPacket(payload), listener);
    }
}
