/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import javax.annotation.Nullable;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IPlayerListExtension {

    default PlayerList self() {
        return (PlayerList) this;
    }

    default void broadcastAll(CustomPacketPayload payload) {
        self().broadcastAll(new ClientboundCustomPayloadPacket(payload));
    }

    default void broadcastAll(CustomPacketPayload payload, ResourceKey<Level> targetLevel) {
        self().broadcastAll(new ClientboundCustomPayloadPacket(payload), targetLevel);
    }

    default void broadcast(
            @Nullable Player excludedPlayer, double x, double y, double z, double range, ResourceKey<Level> level, CustomPacketPayload payload) {
        self().broadcast(excludedPlayer, x, y, z, range, level, new ClientboundCustomPayloadPacket(payload));
    }
}
