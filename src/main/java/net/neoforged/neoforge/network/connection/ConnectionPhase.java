/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public enum ConnectionPhase {
    ANY,
    CONFIGURATION,
    PLAY;

    public boolean isConfiguration() {
        return this == ANY || this == CONFIGURATION;
    }

    public boolean isPlay() {
        return this == ANY || this == PLAY;
    }

    public static ConnectionPhase fromPacketListener(final PacketListener packetListener) {
        if (packetListener instanceof ClientConfigurationPacketListener) {
            return ConnectionPhase.CONFIGURATION;
        }

        if (packetListener instanceof ClientGamePacketListener) {
            return ConnectionPhase.PLAY;
        }

        if (packetListener instanceof ClientCommonPacketListener) {
            return ConnectionPhase.ANY;
        }

        if (packetListener instanceof ServerConfigurationPacketListener) {
            return ConnectionPhase.CONFIGURATION;
        }

        if (packetListener instanceof ServerGamePacketListener) {
            return ConnectionPhase.PLAY;
        }

        if (packetListener instanceof ServerCommonPacketListener) {
            return ConnectionPhase.ANY;
        }

        return ANY;
    }
}
