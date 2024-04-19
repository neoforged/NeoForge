/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.bundle;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PacketAndPayloadAcceptor<L extends ClientCommonPacketListener> {
    private final Consumer<Packet<? super L>> consumer;

    public PacketAndPayloadAcceptor(Consumer<Packet<? super L>> consumer) {
        this.consumer = consumer;
    }

    public PacketAndPayloadAcceptor<L> accept(Packet<? super L> packet) {
        consumer.accept(packet);
        return this;
    }

    public PacketAndPayloadAcceptor<L> accept(CustomPacketPayload payload) {
        return accept(new ClientboundCustomPayloadPacket(payload));
    }
}
