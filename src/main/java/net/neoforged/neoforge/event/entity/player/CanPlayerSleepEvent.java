/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

/// Fired when a player initially attempts to sleep. This can be used by mods to supplement or replace the vanilla logic
/// for whether a sleep attempt is valid.
///
/// Mods which provide a [AbstractBedBlock] without the [`FACING` property][HorizontalDirectionalBlock#FACING] will almost
/// always need to override this method and provide their own logic, because the vanilla logic is modified to assume success
/// for those blocks.
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the [game event bus][NeoForge#EVENT_BUS], only on the [logical server][LogicalSide#SERVER].
///
/// @see ServerPlayer#startSleepInBed(AbstractBedBlock, BlockState, BedRule, BlockPos)
/// @see CanContinueSleepingEvent
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

    /// {@return the position of the bed block}
    public BlockPos getPos() {
        return pos;
    }

    /// {@return the bed block state}
    public BlockState getState() {
        return state;
    }

    /// {@return the current sleeping problem, or `null` if there isn't any problem}
    @Nullable
    public BedSleepingProblem getProblem() {
        return this.problem;
    }

    /// Sets a new sleeping problem, if any. A `null` value means there is no sleeping problem, and the player is allowed to sleep.
    ///
    /// Mods should use a custom [BedSleepingProblem] with an appropriate message if possible. Otherwise, the generic
    /// [BedSleepingProblem#OTHER_PROBLEM] can be used.
    ///
    /// @param problem the new sleeping problem, or `null`
    public void setProblem(@Nullable BedSleepingProblem problem) {
        this.problem = problem;
    }

    /// {@return the sleeping problem provided by the vanilla logic, or `null` if there isn't any problem}
    @Nullable
    public BedSleepingProblem getVanillaProblem() {
        return vanillaProblem;
    }
}
