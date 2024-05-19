/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired before {@link Mob#finalizeSpawn} is called.<br>
 * This allows mods to control mob initialization.<br>
 * In vanilla code, this event is injected by a transformer and not via patch, so calls cannot be traced via call hierarchy (it is not source-visible).
 * <p>
 * Canceling this event will result in {@link Mob#finalizeSpawn} not being called, and the returned value always being null, instead of propagating the SpawnGroupData.<br>
 * The entity will still be spawned. If you want to prevent the spawn, use {@link FinalizeSpawnEvent#setSpawnCancelled}, which will cause Forge to prevent the spawn.
 * <p>
 * This event is fired on {@link NeoForge#EVENT_BUS}, and is only fired on the logical server.
 * 
 * @see EventHooks#onFinalizeSpawn
 * @apiNote Callers do not need to check if the entity's spawn was cancelled, as the spawn will be blocked by Forge.
 */
public class FinalizeSpawnEvent extends MobSpawnEvent implements ICancellableEvent {
    private final MobSpawnType spawnType;

    @Nullable
    private final Either<BlockEntity, Entity> spawner;

    private DifficultyInstance difficulty;

    @Nullable
    private SpawnGroupData spawnData;

    /**
     * @apiNote Fire via {@link EventHooks#onFinalizeSpawn} / {@link EventHooks#onFinalizeSpawnSpawner}.
     */
    @ApiStatus.Internal
    public FinalizeSpawnEvent(Mob entity, ServerLevelAccessor level, double x, double y, double z, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable Either<BlockEntity, Entity> spawner) {
        super(entity, level, x, y, z);
        this.difficulty = difficulty;
        this.spawnType = spawnType;
        this.spawnData = spawnData;
        this.spawner = spawner;
    }

    /**
     * Retrieves the {@link DifficultyInstance} for the chunk where the mob is about to be spawned.
     * 
     * @return The local difficulty instance
     */
    public DifficultyInstance getDifficulty() {
        return this.difficulty;
    }

    /**
     * Sets the difficulty instance for this event, which will be propagated to {@link Mob#finalizeSpawn} unless cancelled.
     * The difficulty instance controls how likely certain random effects are to occur, or if certain mob abilities are enabled.
     * 
     * @param inst The new difficulty instance.
     */
    public void setDifficulty(DifficultyInstance inst) {
        this.difficulty = inst;
    }

    /**
     * Retrieves the type of mob spawn that happened (the event that caused the spawn). The enum names are self-explanatory.
     * 
     * @return The mob spawn type.
     * @see MobSpawnType
     */
    public MobSpawnType getSpawnType() {
        return this.spawnType;
    }

    /**
     * Retrieves the {@link SpawnGroupData} for this entity. When spawning mobs in a loop, this group data is used for the entire group and impacts future spawns.
     * This is how entities like horses ensure that the whole group spawns as a single variant. How this is used varies on a per-entity basis.
     * 
     * @return The spawn group data.
     */
    @Nullable
    public SpawnGroupData getSpawnData() {
        return this.spawnData;
    }

    /**
     * Sets the spawn data for this entity. If this event is cancelled, this value is not used, since {@link Mob#finalizeSpawn} will not be called.
     * 
     * @param data The new spawn data
     * @see FinalizeSpawnEvent#getSpawnData
     */
    public void setSpawnData(@Nullable SpawnGroupData data) {
        this.spawnData = data;
    }

    /**
     * Retrieves the underlying {@link BlockEntity} or {@link Entity} that performed the spawn. This may be a {@link SpawnerBlockEntity},
     * {@link TrialSpawnerBlockEntity}, {@link MinecartSpawner}, or similar modded object.
     * <p>
     * This is usually null unless the {@link #getSpawnType() spawn type} is a {@link MobSpawnType#isSpawner spawner type}, and may still be null even then.
     * 
     * @return The spawner responsible for triggering the spawn, or null if none is available.
     */
    @Nullable
    public Either<BlockEntity, Entity> getSpawner() {
        return this.spawner;
    }

    /**
     * This method can be used to cancel the spawn of this mob.<p>
     * This method must be used if you want to block the spawn, as canceling the event only blocks the call to {@link Mob#finalizeSpawn}.<p>
     * Note that if the spawn is cancelled, but the event is not, then {@link Mob#finalizeSpawn} will still be called, but the entity will not be spawned.
     * Usually that has no side effects, but callers should be aware.
     * 
     * @param cancel If the spawn should be cancelled (or not).
     */
    public void setSpawnCancelled(boolean cancel) {
        this.getEntity().setSpawnCancelled(cancel);
    }

    /**
     * Returns the current spawn cancellation status, which can be changed via {@link FinalizeSpawnEvent#setSpawnCancelled(boolean)}.
     * 
     * @return If this mob's spawn is cancelled or not.
     * @implNote This is enforced in {@link NeoForgeEventHandler#builtinMobSpawnBlocker} and a patch in {@link WorldGenRegion#addEntity}
     */
    public boolean isSpawnCancelled() {
        return this.getEntity().isSpawnCancelled();
    }
}
