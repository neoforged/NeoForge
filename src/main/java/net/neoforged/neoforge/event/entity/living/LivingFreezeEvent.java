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
    private float slowAmount;

    public LivingFreezeEvent(LivingEntity entity, boolean isFreezing) {
        super(entity);
        this.isFreezing = isFreezing;
        this.slowAmount = -0.05F;
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

    public float getSlowAmount() {
        return slowAmount;
    }

    public void setSlowAmount(float slowAmount) {
        this.slowAmount = slowAmount;
    }
}
