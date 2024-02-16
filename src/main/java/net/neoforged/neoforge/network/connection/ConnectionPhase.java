/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.connection;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerPacketListener;

public enum ConnectionPhase {
    COMMON,
    CONFIGURATION,
    PLAY;

    public boolean isConfiguration() {
        return this == COMMON || this == CONFIGURATION;
    }

    public boolean isPlay() {
        return this == COMMON || this == PLAY;
    }

    public static ConnectionPhase fromPacketListener(final PacketListener packetListener) {
        if (packetListener instanceof ClientConfigurationPacketListener) {
            return ConnectionPhase.CONFIGURATION;
        }

        if (packetListener instanceof ClientGamePacketListener) {
            return ConnectionPhase.PLAY;
        }

        if (packetListener instanceof ClientPacketListener) {
            return ConnectionPhase.COMMON;
        }

        if (packetListener instanceof ServerConfigurationPacketListener) {
            return ConnectionPhase.CONFIGURATION;
        }

        if (packetListener instanceof ServerGamePacketListener) {
            return ConnectionPhase.PLAY;
        }

        if (packetListener instanceof ServerPacketListener) {
            return ConnectionPhase.COMMON;
        }

        return COMMON;
    }
}
