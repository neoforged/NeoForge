/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;

/**
 * Fired by {@link PlayerList#respawn(ServerPlayer, boolean)} before the server respawns a player.
 * This may be used to change the {@link ServerLevel} the player respawns in, as well as their respawn position.
 * This event is fired after {@link BlockState#getRespawnPosition(EntityType, LevelReader, BlockPos, float, LivingEntity)} is called.
 * <p>
 * This event is only fired on the logical server.
 */
public class PlayerRespawnPositionEvent extends PlayerEvent {
    private DimensionTransition dimensionTransition;
    private final DimensionTransition originalDimensionTransition;
    private final boolean fromEndFight;
    private boolean changePlayerSpawnPosition = true;

    public PlayerRespawnPositionEvent(ServerPlayer player, DimensionTransition dimensionTransition, boolean fromEndFight) {
        super(player);
        this.dimensionTransition = dimensionTransition;
        this.originalDimensionTransition = dimensionTransition;
        this.fromEndFight = fromEndFight;
    }

    /**
     * @return The dimension transition for where the player will respawn
     */
    public DimensionTransition getDimensionTransition() {
        return dimensionTransition;
    }

    /**
     * Set the dimension transition for where the player will respawn
     * 
     * @param dimensionTransition The new dimension transition.
     */
    public void setDimensionTransition(DimensionTransition dimensionTransition) {
        this.dimensionTransition = dimensionTransition;
    }

    /**
     * Set the level the player will respawn into using a {@link ResourceKey}.
     * 
     * @param respawnLevelResourceKey The {@link ResourceKey} of the level to respawn into.
     */
    public void setRespawnLevel(ResourceKey<Level> respawnLevelResourceKey) {
        MinecraftServer server = Objects.requireNonNull(
                getEntity().getServer(), "The player is not in a ServerLevel somehow?");
        ServerLevel level = Objects.requireNonNull(
                server.getLevel(respawnLevelResourceKey), "Level " + respawnLevelResourceKey + " does not exist!");
        DimensionTransition dt = getDimensionTransition();
        setDimensionTransition(new DimensionTransition(level, dt.pos(), dt.speed(), dt.yRot(), dt.xRot(), dt.postDimensionTransition()));
    }

    /**
     * @return The dimension transition the server originally intended to respawn the player to.
     */
    public DimensionTransition getOriginalDimensionTransition() {
        return originalDimensionTransition;
    }

    /**
     * @return Whether the respawn position will be used as the player's spawn position from then on. Defaults to {@code true}.
     *         {@link PlayerSetSpawnEvent} will be fired if this is {@code true}.
     */
    public boolean changePlayerSpawnPosition() {
        return changePlayerSpawnPosition;
    }

    /**
     * Set whether the respawn position will be used as the player's spawn position from then on.
     * Defaults to {@code true}. {@link PlayerSetSpawnEvent} will be fired if this is {@code true}.
     * 
     * @param changePlayerSpawnPosition Whether to set the player's spawn position.
     */
    public void setChangePlayerSpawnPosition(boolean changePlayerSpawnPosition) {
        this.changePlayerSpawnPosition = changePlayerSpawnPosition;
    }

    /**
     * @return Whether the respawn was triggered by the player jumping into the End return portal.
     * @see ServerPlayer#wonGame
     */
    public boolean isFromEndFight() {
        return fromEndFight;
    }
}
