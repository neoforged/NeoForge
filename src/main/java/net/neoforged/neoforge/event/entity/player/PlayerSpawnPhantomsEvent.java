/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;

/**
 * This event is fired from {@link PhantomSpawner#tick} when phantoms would attempt to be spawned, with one event fired per player.
 * It allows controlling if a spawn attempt will be made for the particular player, but cannot guarantee that a Phantom will be spawned.
 * <p>
 * This event is only fired on the logical server.
 */
public class PlayerSpawnPhantomsEvent extends PlayerEvent {
    private int phantomsToSpawn;
    private Result result = Result.DEFAULT;

    public PlayerSpawnPhantomsEvent(Player player, int phantomsToSpawn) {
        super(player);
        this.phantomsToSpawn = phantomsToSpawn;
    }

    /**
     * @return How many phantoms will be spawned, if spawning is successful. The default value is randomly generated.
     */
    public int getPhantomsToSpawn() {
        return phantomsToSpawn;
    }

    /**
     * Sets the number of phantoms to be spawned.
     * 
     * @param phantomsToSpawn How many phantoms should spawn, given checks are passed.
     */
    public void setPhantomsToSpawn(int phantomsToSpawn) {
        this.phantomsToSpawn = phantomsToSpawn;
    }

    /**
     * Changes the result of this event, which controls if a spawn attempt will be made.
     * 
     * @see PlayerSpawnPhantomsEvent.Result
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * Returns the result of this event, which controls if a spawn attempt will be made.
     * 
     * @see PlayerSpawnPhantomsEvent.Result
     */
    public Result getResult() {
        return result;
    }

    /**
     * Checks if a spawn attempt should be made by checking the current result and evaluating the vanilla conditions if necessary.
     * <p>
     * Does not check {@link DifficultyInstance#isHarderThan(float)} or the player's {@link Stats#TIME_SINCE_REST}, as these are checked later.
     * 
     * @param level The level that phantoms are being spawned in (the same level as the player)
     * @param pos   The block position of the player
     * @return true if a spawn attempt should be made
     */
    public boolean shouldSpawnPhantoms(ServerLevel level, BlockPos pos) {
        if (this.getResult() == Result.ALLOW) {
            return true;
        }
        return this.getResult() == Result.DEFAULT && (!level.dimensionType().hasSkyLight() || pos.getY() >= level.getSeaLevel() && level.canSeeSky(pos));
    }

    /**
     * Controls if the spawn attempt for the group of phantoms will occur.
     * <p>
     * This does not guarantee the spawn attempt will be successful, since this only controls pre-checks
     * before the spawn position is selected and {@link NaturalSpawner#isValidEmptySpawnBlock} is checked.
     */
    public static enum Result {
        /**
         * A spawn attempt will always be made, bypassing all rules described in {@link #DEFAULT}.
         */
        ALLOW,

        /**
         * A spawn attempt will only be made if the dimension does not have skylight
         * <b>or</b> the player's Y-level is above sea level and they can see the sky.
         * <p>
         * Additionally, the local difficulty must be higher than a random threshold in [0, 3)
         * and a random number check based on the player's {@link Stats#TIME_SINCE_REST} must succeed.
         */
        DEFAULT,

        /**
         * A spawn attempt will never be made.
         */
        DENY;
    }
}
