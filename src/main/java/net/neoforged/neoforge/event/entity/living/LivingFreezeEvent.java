/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

public class LivingFreezeEvent extends LivingEvent implements ICancellableEvent {
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

    public boolean isFreezing() {
        return isFreezing;
    }

    public void setFreezing(boolean freezing) {
        isFreezing = freezing;
    }

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
