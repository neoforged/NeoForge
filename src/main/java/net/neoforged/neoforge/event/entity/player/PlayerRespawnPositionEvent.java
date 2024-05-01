/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.Objects;
import javax.annotation.Nullable;
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
import net.minecraft.world.phys.Vec3;

/**
 * Fired by {@link PlayerList#respawn(ServerPlayer, boolean)} before the server respawns a player.
 * This may be used to change the {@link ServerLevel} the player respawns in, as well as their respawn position.
 * This event is fired after {@link BlockState#getRespawnPosition(EntityType, LevelReader, BlockPos, float, LivingEntity)} is called.
 * <p>
 * This event is only fired on the logical server.
 */
public class PlayerRespawnPositionEvent extends PlayerEvent {
    private ServerLevel respawnLevel;
    private @Nullable Vec3 respawnPosition;
    private float respawnAngle;
    private final ServerLevel originalRespawnLevel;
    private final @Nullable Vec3 originalRespawnPosition;
    private final float originalRespawnAngle;
    private final boolean fromEndFight;
    private boolean changePlayerSpawnPosition = true;

    public PlayerRespawnPositionEvent(ServerPlayer player, ServerLevel respawnLevel, float respawnAngle, @Nullable Vec3 respawnPosition, boolean fromEndFight) {
        super(player);
        this.respawnLevel = respawnLevel;
        this.respawnPosition = respawnPosition;
        this.respawnAngle = respawnAngle;
        this.originalRespawnLevel = respawnLevel;
        this.originalRespawnPosition = respawnPosition;
        this.originalRespawnAngle = respawnAngle;
        this.fromEndFight = fromEndFight;
    }

    /**
     * @return The level the player will respawn into.
     */
    public ServerLevel getRespawnLevel() {
        return respawnLevel;
    }

    /**
     * Set the level the player will respawn into.
     * 
     * @param respawnLevel The new level.
     */
    public void setRespawnLevel(ServerLevel respawnLevel) {
        this.respawnLevel = respawnLevel;
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
        setRespawnLevel(level);
    }

    /**
     * @return The level the server originally intended to respawn the player into.
     */
    public ServerLevel getOriginalRespawnLevel() {
        return originalRespawnLevel;
    }

    /**
     * @return The position in the target level where the player will respawn, before any adjustments by the server.
     */
    @Nullable
    public Vec3 getRespawnPosition() {
        return respawnPosition;
    }

    /**
     * Set the player's respawn position within the respawn level. The server automatically adjusts this position
     * to not be inside blocks. If {@code null}, the server will use the default spawn position for the level.
     * 
     * @param respawnPosition
     */
    public void setRespawnPosition(@Nullable Vec3 respawnPosition) {
        this.respawnPosition = respawnPosition;
    }

    /**
     * @return The original position the server intended to respawn the player at.
     */
    @Nullable
    public Vec3 getOriginalRespawnPosition() {
        return originalRespawnPosition;
    }

    /**
     * @return The angle the player will face when they respawn, before any modifications made by the server.
     */
    public float getRespawnAngle() {
        return respawnAngle;
    }

    /**
     * Set the angle the player will face when they respawn. The server may adjust the angle, for example to face
     * a bed if the player respawns there.
     * 
     * @param respawnAngle The angle the player will face when they respawn.
     */
    public void setRespawnAngle(float respawnAngle) {
        this.respawnAngle = respawnAngle;
    }

    /**
     * @return The original angle the server intended for the player to face when they respawn.
     */
    public float getOriginalRespawnAngle() {
        return originalRespawnAngle;
    }

    /**
     * @return Whether the respawn position will be used as the player's spawn position from then on. Defaults to {@code true}.
     *         {@link PlayerSetSpawnEvent} will be fired if this is {@code true}.
     */
    public boolean isChangePlayerSpawnPosition() {
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
