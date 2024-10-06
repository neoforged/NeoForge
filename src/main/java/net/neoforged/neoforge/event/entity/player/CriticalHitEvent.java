/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * This event is fired when a player attacks an entity in {@link Player#attack(Entity)}.
 * <p>
 * It can be used to change the critical hit status and the critical damage multiplier.
 * Additionally, this event allows controlling if the critical hit will impact sweep conditions.
 * <p>
 * This event is fired on both the logical client and logical server.
 */
public class CriticalHitEvent extends PlayerEvent {
    private final Entity target;
    private final float vanillaDmgMultiplier;
    private final boolean isVanillaCritical;

    private float dmgMultiplier;
    private boolean isCriticalHit;
    private boolean disableSweep = true;

    /**
     * Fire via {@link CommonHooks#fireCriticalHit(Player, Entity, boolean, float)}
     */
    public CriticalHitEvent(Player player, Entity target, float dmgMultiplier, boolean isCriticalHit) {
        super(player);
        this.target = target;
        this.dmgMultiplier = this.vanillaDmgMultiplier = dmgMultiplier;
        this.isCriticalHit = this.isVanillaCritical = isCriticalHit;
    }

    /**
     * {@return the entity that was attacked by the player}
     */
    public Entity getTarget() {
        return this.target;
    }

    /**
     * The damage multiplier is applied to the base attack's damage if the attack {@linkplain #isCriticalHit() critically hits}.
     * <p>
     * A damage multiplier of 1.0 will not change the damage, a value of 1.5 will increase the damage by 50%, and so on.
     */
    public float getDamageMultiplier() {
        return this.dmgMultiplier;
    }

    /**
     * Sets the damage multiplier for the critical hit. Not used if {@link #isCriticalHit()} is false.
     * <p>
     * Changing the damage modifier to zero does not guarantee that the attack does zero damage.
     *
     * @param dmgMultiplier The new damage modifier. Must not be negative.
     * @see #getDamageMultiplier()
     */
    public void setDamageMultiplier(float dmgMultiplier) {
        if (dmgMultiplier < 0) {
            throw new UnsupportedOperationException("Attempted to set a negative damage multiplier: " + dmgMultiplier);
        }
        this.dmgMultiplier = dmgMultiplier;
    }

    /**
     * {@return if the attack will critically hit}
     */
    public boolean isCriticalHit() {
        return this.isCriticalHit;
    }

    /**
     * Changes the critical hit state.
     *
     * @param isCriticalHit true if the attack should critically hit
     */
    public void setCriticalHit(boolean isCriticalHit) {
        this.isCriticalHit = isCriticalHit;
    }

    /**
     * Gets the original damage multiplier set by vanilla.
     * <p>
     * If the event {@link #isVanillaCritical()}, the damage multiplier will be 1.5, otherwise it will be 1.0
     *
     * @see #getDamageMultiplier()
     */
    public float getVanillaMultiplier() {
        return this.vanillaDmgMultiplier;
    }

    /**
     * {@return true if the attack was considered a critical hit by vanilla}
     */
    public boolean isVanillaCritical() {
        return this.isVanillaCritical;
    }

    /**
     * Sets if this attack should prevent a sweep from occurring.
     * <p>
     * In vanilla, a critical hit always prevents a sweep from occurring.
     * This method can allow an attack to both critically hit and sweep without having to validate the other sweep conditions.
     * 
     * @see {@link SweepAttackEvent} for more advanced sweep attack handling.
     */
    public void setDisableSweep(boolean disableSweep) {
        this.disableSweep = disableSweep;
    }

    /**
     * If this attack is a {@linkplain #isCriticalHit() critical hit}, returns if a sweep should be prevented.
     * <p>
     * If this attack is <b>not</b> a critical hit, the return value of this method is meaningless.
     * 
     * @see {@link SweepAttackEvent} for more advanced sweep attack handling.
     */
    public boolean disableSweep() {
        return disableSweep;
    }
}
