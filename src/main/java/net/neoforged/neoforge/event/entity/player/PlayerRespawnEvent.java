/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Fired by {@link net.minecraft.server.players.PlayerList#respawn(ServerPlayer, boolean)} when the server respawns a player.
 * This may be used to change the {@link ServerLevel} the player respawns in, as well as their respawn position.
 * This event is fired after {@link net.minecraft.world.level.block.state.BlockState#getRespawnPosition(EntityType, LevelReader, BlockPos, float, LivingEntity)} is called.
 * <p>
 * This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.
 */
public class PlayerRespawnEvent extends PlayerEvent {
    private ServerLevel respawnLevel;
    private @Nullable Vec3 respawnPosition;
    private float respawnAngle;
    private final ServerLevel originalRespawnLevel;
    private final @Nullable Vec3 originalRespawnPosition;
    private final float originalRespawnAngle;
    private final boolean fromEndFight;
    private boolean changePlayerSpawnPosition = false;

    public PlayerRespawnEvent(ServerPlayer player, ServerLevel respawnLevel, float respawnAngle, @Nullable Vec3 respawnPosition, boolean fromEndFight) {
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
     * The level the player will respawn into
     */
    public ServerLevel getRespawnLevel() {
        return respawnLevel;
    }

    public void setRespawnLevel(ServerLevel respawnLevel) {
        this.respawnLevel = respawnLevel;
    }

    public ServerLevel getOriginalRespawnLevel() {
        return originalRespawnLevel;
    }

    /**
     * @return The position in the target level where the player will respawn. The server automatically adjusts this position
     *         to not be inside blocks. If {@code null}, the server will use the default spawn for the level.
     */
    @Nullable
    public Vec3 getRespawnPosition() {
        return respawnPosition;
    }

    public void setRespawnPosition(@Nullable Vec3 respawnPosition) {
        this.respawnPosition = respawnPosition;
    }

    @Nullable
    public Vec3 getOriginalRespawnPosition() {
        return originalRespawnPosition;
    }

    /**
     * @return The angle the player will face when they respawn.
     */
    public float getRespawnAngle() {
        return respawnAngle;
    }

    public void setRespawnAngle(float respawnAngle) {
        this.respawnAngle = respawnAngle;
    }

    public float getOriginalRespawnAngle() {
        return originalRespawnAngle;
    }

    /**
     * @return Whether the player's spawn will be changed to the respawn position.
     *         {@link PlayerSetSpawnEvent} will be fired if this is {@code true}.
     */
    public boolean isChangePlayerSpawnPosition() {
        return changePlayerSpawnPosition;
    }

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
