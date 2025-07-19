/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;

/**
 * LivingFreezeEvent is fired whenever a living entity ticks.<br>
 * <br>
 * This event is fired via {@link CommonHooks#onLivingFreeze(LivingEntity, ServerLevel)}.<br>
 * <br>
 * This event is {@link ICancellableEvent}.<br>
 * <br>
 * This event is fired on {@link NeoForge#EVENT_BUS}, on the logical server only.
 */
public class LivingFreezeEvent extends LivingEvent {
    private boolean isFreezing;
    private int ticksRequiredToFreeze;
    private float slowAmount;
    private float damageAmount;
    private int damageTickRate; // In ticks

    public LivingFreezeEvent(LivingEntity entity, boolean isFreezing) {
        super(entity);
        this.isFreezing = isFreezing;
        this.ticksRequiredToFreeze = entity.getTicksRequiredToFreeze();
        this.slowAmount = -0.05F;
        this.damageAmount = 1.0F;
        this.damageTickRate = 40;
    }

    /**
     * If the entity is freezing, its freezing counter will be increased. If it's over the
     * {@link #getTicksRequiredToFreeze()} threshold, the entity will take damage.<br>
     * If the entity is not freezing, its freezing counter will be decreased.
     *
     * @return True if the entity is freezing
     */
    public boolean isFreezing() {
        return isFreezing;
    }

    /**
     * Sets if the entity is freezing or not.
     *
     * @param freezing The new value.
     */
    public void setFreezing(boolean freezing) {
        isFreezing = freezing;
    }

    /**
     * @return The number of ticks needed to fully freeze (start applying damage) while freezing.
     */
    public int getTicksRequiredToFreeze() {
        return ticksRequiredToFreeze;
    }

    public void setTicksRequiredToFreeze(int ticksRequiredToFreeze) {
        this.ticksRequiredToFreeze = ticksRequiredToFreeze;
    }

    public float getSlowAmount() {
        return slowAmount;
    }

    public void setSlowAmount(float slowAmount) {
        this.slowAmount = slowAmount;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public void setDamageAmount(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    public int getDamageTickRate() {
        return damageTickRate;
    }

    public void setDamageTickRate(int damageTickRate) {
        this.damageTickRate = damageTickRate;
    }
}
