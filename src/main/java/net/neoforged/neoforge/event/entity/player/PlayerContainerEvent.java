/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class PlayerContainerEvent extends ServerPlayerEvent {
    private final AbstractContainerMenu container;

    public PlayerContainerEvent(ServerPlayer player, AbstractContainerMenu container) {
        super(player);
        this.container = container;
    }

    public AbstractContainerMenu getContainer() {
        return container;
    }

    public static class Open extends PlayerContainerEvent {
        public Open(ServerPlayer player, AbstractContainerMenu container) {
            super(player, container);
        }
    }

    public static class Close extends PlayerContainerEvent {
        public Close(ServerPlayer player, AbstractContainerMenu container) {
            super(player, container);
        }
    }
}
