/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
    private float slowAmount;
    private int ticksRequiredToFreeze;

    public LivingFreezeEvent(LivingEntity entity, boolean isFreezing) {
        super(entity);
        this.isFreezing = isFreezing;
        this.slowAmount = -0.05F;
        this.ticksRequiredToFreeze = entity.getTicksRequiredToFreeze();
    }

    /**
     * If the entity is freezing, its freezing counter will be increased. If it's over the
     * {@link LivingEntity#getTicksRequiredToFreeze()} threshold, the entity will take damage.<br>
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
     * Get the slow attribute value applied to the entity as it's freezing. It's applied as {@link AttributeModifier.Operation#ADD_VALUE}.
     * 
     * @return The current value
     */
    public float getSlowAmount() {
        return slowAmount;
    }

    /**
     * Sets the slow attribute value that will be applied to the entity as it's freezing.
     * 
     * @param slowAmount The new value.
     */
    public void setSlowAmount(float slowAmount) {
        this.slowAmount = slowAmount;
    }

    /**
     * Get the amount of ticks required to fully freeze (start taking damage).
     *
     * @return The current value
     */
    public int getTicksRequiredToFreeze() {
        return ticksRequiredToFreeze;
    }

    /**
     * Sets the amount of ticks required to fully freeze (start taking damage).
     *
     * @param ticksRequiredToFreeze The new value
     */
    public void setTicksRequiredToFreeze(int ticksRequiredToFreeze) {
        this.ticksRequiredToFreeze = ticksRequiredToFreeze;
    }
}
