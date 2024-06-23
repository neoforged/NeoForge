/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;

public abstract class ServerPlayerEvent extends PlayerEvent {
    protected ServerPlayerEvent(ServerPlayer entity) {
        super(entity);
    }

    public ServerPlayer getEntity() {
        return ((ServerPlayer) super.getEntity());
    }
}
