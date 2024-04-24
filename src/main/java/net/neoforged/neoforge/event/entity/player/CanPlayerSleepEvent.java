/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Called from {@link ServerPlayer#startSleepInBed(BlockPos)} when a player attempts to sleep.
 * <p>
 * This event receives the result of vanilla checking if the sleep attempt is valid, and permits overriding it.
 * <p>
 * This event is only fired on the logical server.
 * 
 * @see {@link CanContinueSleepingEvent} for per-tick sleeping checks.
 */
public class CanPlayerSleepEvent extends PlayerEvent {
    private final BlockPos pos;
    private final BlockState state;

    @Nullable
    private final BedSleepingProblem vanillaProblem;

    @Nullable
    private BedSleepingProblem problem;

    public CanPlayerSleepEvent(ServerPlayer player, BlockPos pos, @Nullable BedSleepingProblem problem) {
        super(player);
        this.pos = pos;
        this.state = player.level().getBlockState(pos);
        this.vanillaProblem = this.problem = problem;
    }

    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }

    public Level getLevel() {
        return this.getEntity().level();
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    /**
     * {@return the current sleeping problem}
     */
    @Nullable
    public BedSleepingProblem getProblem() {
        return this.problem;
    }

    /**
     * Sets a new sleeping problem. If the new problem is null, the player is allowed to sleep here.
     */
    public void setProblem(@Nullable BedSleepingProblem problem) {
        this.problem = problem;
    }

    /**
     * Returns the default sleeping problem based on the vanilla checks.
     * 
     * @see ServerPlayer#startSleepInBed(BlockPos) to identify the cause of a problem.
     */
    @Nullable
    public BedSleepingProblem getVanillaProblem() {
        return vanillaProblem;
    }
}
