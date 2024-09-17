/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * The SweepAttackEvent is fired when a {@link Player} attacks a target, after the {@link CriticalHitEvent} has been fired.
 * <p>
 * This event can be used to force an attack to trigger a sweep, or to prevent a sweep from occurring.
 * <p>
 * This event is fired on both the logical client and logical server.
 */
public class SweepAttackEvent extends PlayerEvent implements ICancellableEvent {
    private final Entity target;
    private final boolean isVanillaSweep;

    private boolean isSweeping;

    public SweepAttackEvent(Player player, Entity target, boolean isVanillaSweep) {
        super(player);
        this.target = target;
        this.isSweeping = this.isVanillaSweep = isVanillaSweep;
    }

    /**
     * Returns the target of the attack, which is guaranteed to be a valid attack target.
     */
    public Entity getTarget() {
        return this.target;
    }

    /**
     * Returns true if the attack would cause a sweep by utilizing the vanilla rules.
     * <p>
     * The vanilla rules are as follows. All of them must be true for a vanilla sweep to occur:
     * <ol>
     * <li>The player's attack strength is greater than 90%.</li>
     * <li>The attack is not a critical hit, or is a critical hit which does not {@linkplain CriticalHitEvent#disableSweep() disable the sweep attack}.</li>
     * <li>The player is on the ground.</li>
     * <li>The distance the player has traveled this tick is less than their speed.</li>
     * <li>The player's weapon supports sweep attacks via {@link ItemAbilities#SWORD_SWEEP}.</li>
     * </ol>
     */
    public boolean isVanillaSweep() {
        return this.isVanillaSweep;
    }

    /**
     * Returns true if the attack will be trigger a sweep.
     */
    public boolean isSweeping() {
        return this.isSweeping;
    }

    /**
     * @param sweep Whether to enable a sweep for this attack.
     */
    public void setSweeping(boolean sweep) {
        this.isSweeping = sweep;
    }

    /**
     * Cancels the event, preventing further event handlers from acting. Canceling the event will use the current value of {@link #isSweeping()}.
     * <p>
     * If you intend to perform a custom sweep attack, you should cancel the event and {@link #setSweeping} to false before performing your handling.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
