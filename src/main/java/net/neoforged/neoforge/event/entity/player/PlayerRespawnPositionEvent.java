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
import net.minecraft.world.level.portal.TeleportTransition;

/**
 * Fired by {@link PlayerList#respawn(ServerPlayer, boolean)} before the server respawns a player.
 * This may be used to change the {@link ServerLevel} the player respawns in, as well as their respawn position.
 * This event is fired after {@link BlockState#getRespawnPosition(EntityType, LevelReader, BlockPos, float, LivingEntity)} is called.
 * <p>
 * This event is only fired on the logical server.
 */
public class PlayerRespawnPositionEvent extends PlayerEvent {
    private TeleportTransition teleportTransition;
    private final TeleportTransition originalTeleportTransition;
    private final boolean fromEndFight;
    private boolean copyOriginalSpawnPosition;

    public PlayerRespawnPositionEvent(ServerPlayer player, TeleportTransition teleportTransition, boolean fromEndFight) {
        super(player);
        this.teleportTransition = teleportTransition;
        this.originalTeleportTransition = teleportTransition;
        this.fromEndFight = fromEndFight;
        this.copyOriginalSpawnPosition = !this.originalTeleportTransition.missingRespawnBlock();
    }

    /**
     * @return The teleport transition for where the player will respawn
     */
    public TeleportTransition getTeleportTransition() {
        return teleportTransition;
    }

    /**
     * Set the teleport transition for where the player will respawn
     * 
     * @param teleportTransition The new teleport transition.
     */
    public void setTeleportTransition(TeleportTransition teleportTransition) {
        this.teleportTransition = teleportTransition;
    }

    /**
     * Set the level the player will respawn into using a {@link ResourceKey}.
     * 
     * @param respawnLevelResourceKey The {@link ResourceKey} of the level to respawn into.
     */
    public void setRespawnLevel(ResourceKey<Level> respawnLevelResourceKey) {
        MinecraftServer server = Objects.requireNonNull(
                getEntity().level().getServer(), "The player is not in a ServerLevel somehow?");
        ServerLevel level = Objects.requireNonNull(
                server.getLevel(respawnLevelResourceKey), "Level " + respawnLevelResourceKey + " does not exist!");
        TeleportTransition dt = getTeleportTransition();
        setTeleportTransition(new TeleportTransition(level, dt.position(), dt.deltaMovement(), dt.yRot(), dt.xRot(), dt.relatives(), dt.postTeleportTransition()));
    }

    /**
     * @return The teleport transition the server originally intended to respawn the player to.
     */
    public TeleportTransition getOriginalTeleportTransition() {
        return originalTeleportTransition;
    }

    /**
     * If the respawn position of the original player will be copied to the fresh player via {@link ServerPlayer#copyRespawnPosition(ServerPlayer)}.
     * <p>
     * This defaults to true if the {@linkplain #getOriginalTeleportTransition() original teleport transition}
     * was not {@linkplain TeleportTransition#missingRespawnBlock() missing a respawn block}.
     * <p>
     * This has no impact on the selected position for the current respawn, but controls if the player will (for example) retain their bed as their set respawn position.
     */
    public boolean copyOriginalSpawnPosition() {
        return copyOriginalSpawnPosition;
    }

    /**
     * Changes if the original player's respawn position will be copied to the fresh player via {@link ServerPlayer#copyRespawnPosition(ServerPlayer)}.
     * <p>
     * If you wish to modify the set respawn position of the fresh player (for future respawns, not the current respawn), you can
     * change the respawn position of the {@linkplain #getEntity() current player} and set this value to true.
     * 
     * @see #copyOriginalSpawnPosition()
     */
    public void setCopyOriginalSpawnPosition(boolean copyOriginalSpawnPosition) {
        this.copyOriginalSpawnPosition = copyOriginalSpawnPosition;
    }

    /**
     * @return Whether the respawn was triggered by the player jumping into the End return portal.
     * @see ServerPlayer#wonGame
     */
    public boolean isFromEndFight() {
        return fromEndFight;
    }
}
