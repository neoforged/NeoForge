/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class PlayerChangedDimensionEvent extends ServerPlayerEvent {
    private final ResourceKey<Level> originalDimension;
    private final ResourceKey<Level> newDimension;
    public PlayerChangedDimensionEvent(ServerPlayer player, ResourceKey<Level> originalDimension, ResourceKey<Level> newDimension) {
        super(player);
        this.originalDimension = originalDimension;
        this.newDimension = newDimension;
    }

    public ResourceKey<Level> getOriginalDimension() {
        return originalDimension;
    }

    public ResourceKey<Level> getNewDimension() {
        return newDimension;
    }
}
