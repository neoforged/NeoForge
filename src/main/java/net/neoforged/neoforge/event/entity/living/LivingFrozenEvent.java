/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;

/**
 * LivingDrownEvent is fired whenever a living entity is fully frozen and is taking damage.
 * <p>
 * This event is fired via {@link CommonHooks#onLivingFreeze(LivingEntity, ServerLevel)}.
 * <p>
 * This event is {@link ICancellableEvent}. Effects of cancellation are noted in {@link #setCanceled(boolean)}.
 * <p>
 * This event does not {@linkplain HasResult have a result}.
 * This event is fired on {@link NeoForge#EVENT_BUS}
 **/
public class LivingFrozenEvent extends LivingEvent implements ICancellableEvent {
    private float damageAmount;
    private int damageTickInterval;

    public LivingFrozenEvent(LivingEntity entity) {
        super(entity);
        this.damageAmount = 1.0F;
        this.damageTickInterval = 40;
    }

    /**
     * Gets the amount of {@linkplain DamageSources#freeze() drowning damage} the entity would take.<br>
     * For vanilla entities, the default amount of damage is 1 (half a heart).
     * <p>
     * If the damage amount is less than or equal to zero, {@link Entity#hurtServer} will not be called.
     *
     * @return The amount of damage that will be dealt to the entity when actively drowning.
     */
    public float getDamageAmount() {
        return damageAmount;
    }

    /**
     * Sets the amount of freezing damage that may be inflicted.
     *
     * @param damageAmount The new value.
     * @see #getDamageAmount()
     */
    public void setDamageAmount(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    /**
     * @return The amount of ticks between two damages instances.
     */
    public int getDamageTickInterval() {
        return damageTickInterval;
    }

    /**
     * Sets the amount of ticks between two damages instances.
     *
     * @param damageTickInterval The new value.
     */
    public void setDamageTickInterval(int damageTickInterval) {
        this.damageTickInterval = damageTickInterval;
    }

    /**
     * Cancelling the event will cancel the damage to the entity.
     * 
     * @param canceled
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
