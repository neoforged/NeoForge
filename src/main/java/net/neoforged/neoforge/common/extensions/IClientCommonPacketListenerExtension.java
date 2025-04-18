/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Extension interface for {@link ClientCommonPacketListener}
 */
public interface IClientCommonPacketListenerExtension extends ICommonPacketListener {
    /**
     * {@inheritDoc}
     */
    @Override
    default void send(CustomPacketPayload payload) {
        this.send(new ServerboundCustomPayloadPacket(payload));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void disconnect(Component reason) {
        this.getConnection().disconnect(reason);
    }
}
