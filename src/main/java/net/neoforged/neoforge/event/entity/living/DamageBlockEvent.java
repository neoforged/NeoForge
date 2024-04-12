/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * The ShieldBlockEvent is fired when an entity is hurt and vanilla checks if the entity is attempting
 * to block with a shield.<br>
 * Cancelling this event will have the same impact as if the shield was not eligible to block.<br>
 * The damage blocked cannot be set lower than zero or greater than the original value.<br>
 * <h4>Note: This event fires whether the player is actively using a shield or not. Vanilla shield
 * blocking logic is captured and passed into the event via {@link #getOriginalBlock()}. If this is
 * true, The shield item stack "should" be available from {@link LivingEntity#getUseItem()} at least
 * for players.</h4>
 */
public class DamageBlockEvent extends DamageSequenceEvent implements ICancellableEvent {
    private float dmgBlocked;
    private float shieldDamage = -1;
    private final boolean originalBlocked;
    private boolean newBlocked;

    public DamageBlockEvent(LivingEntity blocker, DamageContainer container, boolean originalBlockedState) {
        super(blocker, container);
        this.dmgBlocked = container.getNewDamage();
        this.originalBlocked = originalBlockedState;
        this.newBlocked = originalBlockedState;
        this.shieldDamage = container.getNewDamage();
    }

    /**
     * @return The damage source.
     */
    public DamageSource getDamageSource() {
        return this.getDamageContainer().getSource();
    }

    /**
     * @return The original amount of damage blocked, which is the same as the original
     *         incoming damage value.
     */
    public float getOriginalBlockedDamage() {
        return this.getDamageContainer().getNewDamage();
    }

    /**
     * @return The current amount of damage blocked, as a result of this event.
     */
    public float getBlockedDamage() {
        return Math.min(this.dmgBlocked, container.getNewDamage());
    }

    /**
     * If the event is {@link #getBlocked()} and the user is holding a shield, the returned amount
     * will be taken from the item's durability.
     * 
     * @return The amount of shield durability damage to take.
     */
    public float shieldDamage() {
        if (newBlocked)
            return shieldDamage >= 0 ? shieldDamage : getBlockedDamage();
        return 0;
    }

    /**
     * Set how much damage is blocked by this action.<br>
     * Note that initially the blocked amount is the entire attack.<br>
     */
    public void setBlockedDamage(float blocked) {
        this.dmgBlocked = Mth.clamp(blocked, 0, this.getOriginalBlockedDamage());
    }

    /**
     * Set how much durability the shield will lose if {@link #getBlocked()} is true.
     *
     * @param damage the new durability value taken from the shield on successful block
     */
    public void setShieldDamage(float damage) {
        this.shieldDamage = damage;
    }

    /**
     * @return whether the damage would have been blocked by vanilla logic
     */
    public boolean getOriginalBlock() {
        return originalBlocked;
    }

    /**
     * Used in {@link LivingEntity#hurt(DamageSource, float)} to signify that a blocking
     * action has occurred. If returning false, damage to the shield will not occur.
     *
     * @return true if the entity should be considered "blocking"
     */
    public boolean getBlocked() {
        return newBlocked;
    }

    /**
     * Sets the blocking state of the entity. By default, entities raising a shield,
     * facing the damage source, and not being hit by a source that bypasses shields
     * will be considered blocking. An entity can be considered blocking regardless
     * by supplying true to this.
     *
     * @param isBlocked should the entity be treated as if it is blocking
     */
    public void setBlocked(boolean isBlocked) {
        this.newBlocked = isBlocked;
    }
}
