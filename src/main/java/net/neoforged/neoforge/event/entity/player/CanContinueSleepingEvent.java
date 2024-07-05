/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired when the game checks if a sleeping entity may continue sleeping.
 * <p>
 * It can be used to overwrite the vanilla check, forcing the entity to continue or stop sleeping.
 * <p>
 * This event is only fired on the logical server.
 * 
 * @see {@link CanPlayerSleepEvent} for when a player starts sleeping.
 */
public class CanContinueSleepingEvent extends LivingEvent {
    @Nullable
    protected final BedSleepingProblem problem;
    protected boolean mayContinueSleeping;

    public CanContinueSleepingEvent(LivingEntity entity, @Nullable BedSleepingProblem problem) {
        super(entity);
        this.problem = problem;
        this.mayContinueSleeping = (problem == null);
    }

    /**
     * Returns the sleeping position of the sleeping entity. May be empty.
     */
    Optional<BlockPos> getSleepingPos() {
        return this.getEntity().getSleepingPos();
    }

    /**
     * Returns the current sleeping problem, if any. By default, this event is fired with the following problems:
     * <ul>
     * <li>{@link BedSleepingProblem#NOT_POSSIBLE_HERE} if the sleeper is missing a bed.</li>
     * <li>{@link BedSleepingProblem#NOT_POSSIBLE_NOW} if it is daytime.</li>
     * </ul>
     * 
     * Mods may fire this event with other problems if they impose additional sleeping conditions.
     */
    @Nullable
    public BedSleepingProblem getProblem() {
        return this.problem;
    }

    /**
     * {@return if the sleeping entity may continue sleeping}
     */
    public boolean mayContinueSleeping() {
        return this.mayContinueSleeping;
    }

    /**
     * Sets if the sleeping entity may continue sleeping.
     * By default, the entity may continue sleeping if there was not a problem detected.
     */
    public void setContinueSleeping(boolean sleeping) {
        this.mayContinueSleeping = sleeping;
    }
}
