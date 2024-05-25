/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.SpawnData.CustomSpawnRules;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.SpawnPlacementRegisterEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * This class holds all events relating to the entire flow of mob spawns.<br>
 * Currently, the events have the following flow for any given mob spawn:
 * <p>
 * Before the spawn is attempted {@link SpawnPlacementCheck} is fired, to determine if the spawn may occur based on mob-specific rules.<br>
 * After the entity is created {@link PositionCheck} is fired, to determine if the selected position is legal for the entity.<br>
 * If both checks succeeded, {@link FinalizeSpawn} is fired, which performs initialization on the newly-spawned entity.<br>
 * Finally, if the spawn was not cancelled via {@link FinalizeSpawn#setSpawnCancelled}, then {@link EntityJoinLevelEvent} is fired as the entity enters the world.
 * <p>
 * {@link AllowDespawn} is not related to the mob spawn event flow, as it fires when a despawn is attempted.
 */
public abstract class MobSpawnEvent extends EntityEvent {
    private final ServerLevelAccessor level;
    private final double x;
    private final double y;
    private final double z;

    @ApiStatus.Internal
    protected MobSpawnEvent(Mob mob, ServerLevelAccessor level, double x, double y, double z) {
        super(mob);
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Mob getEntity() {
        return (Mob) super.getEntity();
    }

    /**
     * @return The level relating to the mob spawn action
     */
    public ServerLevelAccessor getLevel() {
        return this.level;
    }

    /**
     * @return The x-coordinate relating to the mob spawn action
     */
    public double getX() {
        return this.x;
    }

    /**
     * @return The y-coordinate relating to the mob spawn action
     */
    public double getY() {
        return this.y;
    }

    /**
     * @return The z-coordinate relating to the mob spawn action
     */
    public double getZ() {
        return this.z;
    }

    /**
     * This event is fired {@linkplain SpawnPlacements#checkSpawnRules when Spawn Placements (aka Spawn Rules) are checked}, before a mob attempts to spawn.<br>
     * Spawn Placement checks include light levels, slime chunks, grass blocks for animals, and others in the same vein.<br>
     * The purpose of this event is to permit runtime changes to any or all spawn placement logic without having to wrap the placement for each entity.
     * <p>
     * This event is only fired on the {@linkplain LogicalSide#SERVER logical server}.
     * <p>
     * This event is not fired for mob spawners which utilize {@link CustomSpawnRules}, as they do not check spawn placements.
     * 
     * @apiNote If your modifications are for a single entity, and do not vary at runtime, use {@link SpawnPlacementRegisterEvent}.
     * @see SpawnPlacementRegisterEvent
     */
    public static class SpawnPlacementCheck extends Event {
        private final EntityType<?> entityType;
        private final ServerLevelAccessor level;
        private final MobSpawnType spawnType;
        private final BlockPos pos;
        private final RandomSource random;
        private final boolean defaultResult;
        private Result result;

        /**
         * Internal.
         * 
         * @see {@link SpawnPlacements#checkSpawnRules} for the single call site of this event.
         */
        @ApiStatus.Internal
        public SpawnPlacementCheck(EntityType<?> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, boolean defaultResult) {
            this.entityType = entityType;
            this.level = level;
            this.spawnType = spawnType;
            this.pos = pos;
            this.random = random;
            this.defaultResult = defaultResult;
        }

        /**
         * @return The type of entity that checks are being performed for.
         */
        public EntityType<?> getEntityType() {
            return this.entityType;
        }

        /**
         * @return The level relating to the mob spawn action
         */
        public ServerLevelAccessor getLevel() {
            return this.level;
        }

        /**
         * Retrieves the type of mob spawn that is happening.
         * 
         * @return The mob spawn type.
         * @see MobSpawnType
         */
        public MobSpawnType getSpawnType() {
            return this.spawnType;
        }

        /**
         * @return The position where checks are being evaluated.
         */
        public BlockPos getPos() {
            return this.pos;
        }

        /**
         * In all vanilla cases, this is equal to {@link ServerLevelAccessor#getRandom()}.
         * 
         * @return The random source being used.
         */
        public RandomSource getRandom() {
            return this.random;
        }

        /**
         * The default vanilla result is useful if an additional check wants to force {@link Result#ALLOW} only if the vanilla check would succeed.
         * 
         * @return The result of the vanilla spawn placement check.
         */
        public boolean getDefaultResult() {
            return this.defaultResult;
        }

        /**
         * Changes the result of this event.
         * 
         * @see {@link Result} for the possible states.
         */
        public void setResult(Result result) {
            this.result = result;
        }

        /**
         * {@return the result of this event, which controls if the placement check will succeed}
         */
        public Result getResult() {
            return this.result;
        }

        /**
         * {@return If the placement check will succeed or not, based on the current event result}
         */
        public boolean getPlacementCheckResult() {
            if (this.result == Result.SUCCEED) {
                return true;
            }
            return this.result == Result.DEFAULT && this.getDefaultResult();
        }

        public static enum Result {
            /**
             * Forces the event to cause the placement check to succeed.
             */
            SUCCEED,

            /**
             * The result of {@link SpawnPredicate#test(EntityType, ServerLevelAccessor, MobSpawnType, BlockPos, RandomSource)} will be used to determine if the check will succeed.
             * <p>
             * If the mob does not have a spawn predicate, the check will always succeed.
             * 
             * @see {@link SpawnPlacementCheck#getDefaultResult()}
             */
            DEFAULT,

            /**
             * Forces the event to cause the placement check to fail.
             */
            FAIL;
        }
    }

    /**
     * This event is fired when a mob checks for a valid spawn position, after {@link SpawnPlacements#checkSpawnRules} has been evaluated.<br>
     * Conditions validated here include the following:
     * <ul>
     * <li>Obstruction - mobs inside blocks or fluids.</li>
     * <li>Pathfinding - if the spawn block is valid for pathfinding.</li>
     * <li>Sea Level - Ocelots check if the position is above sea level.</li>
     * <li>Spawn Block - Ocelots check if the below block is grass or leaves.</li>
     * </ul>
     * <p>
     * This event is only fired on the {@linkplain LogicalSide#SERVER logical server}.
     *
     * @apiNote This event fires after Spawn Placement checks, which are the primary set of spawn checks.
     * @see {@link SpawnPlacementRegisterEvent} To modify spawn placements statically at startup.
     * @see {@link SpawnPlacementCheck} To modify the result of spawn placements at runtime.
     */
    public static class PositionCheck extends MobSpawnEvent {
        @Nullable
        private final BaseSpawner spawner;
        private final MobSpawnType spawnType;
        private Result result;

        public PositionCheck(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, @Nullable BaseSpawner spawner) {
            super(mob, level, mob.getX(), mob.getY(), mob.getZ());
            this.spawnType = spawnType;
            this.spawner = spawner;
        }

        /**
         * Retrieves the underlying {@link BaseSpawner} instance if this mob was created by a Mob Spawner of some form.
         * This is always null unless {@link #getSpawnType()} is {@link MobSpawnType#SPAWNER}, and may still be null even then.
         * 
         * @return The BaseSpawner responsible for triggering the spawn, or null if none is available.
         */
        @Nullable
        public BaseSpawner getSpawner() {
            return spawner;
        }

        /**
         * Retrieves the type of mob spawn that is happening.
         * 
         * @return The mob spawn type.
         * @see MobSpawnType
         */
        public MobSpawnType getSpawnType() {
            return this.spawnType;
        }

        /**
         * Changes the result of this event.
         * 
         * @see {@link Result} for the possible states.
         */
        public void setResult(Result result) {
            this.result = result;
        }

        /**
         * {@return the result of this event, which controls if the position check will succeed}
         */
        public Result getResult() {
            return this.result;
        }

        public static enum Result {
            /**
             * Forces the event to cause the position check to succeed.
             */
            SUCCEED,

            /**
             * The results of {@link Mob#checkSpawnRules(LevelAccessor, MobSpawnType)} and {@link Mob#checkSpawnObstruction(LevelReader)} will be used to determine if the check will succeed.
             * <p>
             * If this is being called from a spawner, the {@link Mob#checkSpawnRules(LevelAccessor, MobSpawnType)} call will be skipped if any {@link CustomSpawnRules} are present.
             */
            DEFAULT,

            /**
             * Forces the event to cause the position check to fail.
             */
            FAIL;
        }
    }
}
