/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;

public class PlayerLoggedOutEvent extends ServerPlayerEvent {
    public PlayerLoggedOutEvent(ServerPlayer entity) {
        super(entity);
    }
}
