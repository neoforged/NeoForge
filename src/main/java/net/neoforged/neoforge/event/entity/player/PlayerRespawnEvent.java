/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;

public class PlayerRespawnEvent extends ServerPlayerEvent {
    private final boolean endConquered;

    public PlayerRespawnEvent(ServerPlayer player, boolean endConquered) {
        super(player);
        this.endConquered = endConquered;
    }

    /**
     * Did this respawn event come from the player conquering the end?
     *
     * @return if this respawn was because the player conquered the end
     */
    public boolean isEndConquered() {
        return this.endConquered;
    }
}
