/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when a player's spawn point is set or reset.<br>
 * The event can be canceled, which will prevent the spawn point from being changed.
 */
public class PlayerSetSpawnEvent extends PlayerEvent implements ICancellableEvent {
    private final ResourceKey<Level> spawnLevel;
    private final boolean forced;
    @Nullable
    private final BlockPos newSpawn;

    public PlayerSetSpawnEvent(Player player, ResourceKey<Level> spawnLevel, @Nullable BlockPos newSpawn, boolean forced) {
        super(player);
        this.spawnLevel = spawnLevel;
        this.newSpawn = newSpawn;
        this.forced = forced;
    }

    public boolean isForced() {
        return forced;
    }

    /**
     * The new spawn position, or null if the spawn position is being reset.
     */
    @Nullable
    public BlockPos getNewSpawn() {
        return newSpawn;
    }

    public ResourceKey<Level> getSpawnLevel() {
        return spawnLevel;
    }
}
