/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.List;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.apache.commons.compress.utils.Lists;

/**
 * Extension class for {@link ServerGamePacketListener}
 */
public interface IServerGamePacketListenerExtension extends IServerCommonPacketListenerExtension {

    /**
     * {@return the listener this extension is attached to}
     */
    ServerGamePacketListener self();

    /**
     * Sends all given payloads as a bundle to the client.
     *
     * @param payloads the payloads to send
     */
    default void sendBundled(CustomPacketPayload... payloads) {
        this.sendBundled(List.of(payloads));
    }

    /**
     * Sends all given payloads as a bundle to the client.
     *
     * @param payloads the payloads to send
     */
    default void sendBundled(Iterable<CustomPacketPayload> payloads) {
        final List<Packet<? super ClientGamePacketListener>> packets = Lists.newArrayList();
        for (CustomPacketPayload payload : payloads) {
            packets.add(new ClientboundCustomPayloadPacket(payload));
        }

        self().send(new ClientboundBundlePacket(packets));
    }
}
