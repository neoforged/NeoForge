/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * LivingBreatheEvent is fired whenever a living entity ticks.<br>
 * <br>
 * This event is fired via {@link ForgeHooks#onLivingBreathe(LivingEntity, int, int)}.<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}
 * <br>
 * This event is fired on {@link MinecraftForge#EVENT_BUS}
 */
public class LivingBreatheEvent extends LivingEvent
{
    private boolean canBreathe;
    private int consumeAirAmount;
    private int refillAirAmount;

    public LivingBreatheEvent(LivingEntity entity, boolean canBreathe, int consumeAirAmount, int refillAirAmount)
    {
        super(entity);
        this.canBreathe = canBreathe;
        this.consumeAirAmount = Math.max(consumeAirAmount, 0);
        this.refillAirAmount = Math.max(refillAirAmount, 0);
    }

    /**
     * If the entity can breathe, their air value will be increased by {@link #getRefillAirAmount()}.<br>
     * If the entity cannot breathe, their air value will be reduced by {@link #getConsumeAirAmount()}.
     * @return True if the entity can breathe
     */
    public boolean canBreathe()
    {
        return canBreathe;
    }

    /**
     * Sets if the entity can breathe or not.
     * @param canBreathe The new value.
     */
    public void setCanBreathe(boolean canBreathe)
    {
        this.canBreathe = canBreathe;
    }

    /**
     * @return The amount the entity's {@linkplain LivingEntity#getAirSupply() air supply} will be reduced by if the entity {@linkplain #canBreathe() cannot breathe}.
     */
    public int getConsumeAirAmount()
    {
        return consumeAirAmount;
    }

    /**
     * Sets the new consumed air amount.
     * @param consumeAirAmount The new value.
     * @see #getConsumeAirAmount()
     */
    public void setConsumeAirAmount(int consumeAirAmount)
    {
        this.consumeAirAmount = Math.max(consumeAirAmount, 0);
    }

    /**
     * @return The amount the entity's {@linkplain LivingEntity#getAirSupply() air supply} will be increased by if the entity {@linkplain #canBreathe() can breathe}.
     */
    public int getRefillAirAmount()
    {
        return refillAirAmount;
    }

    /**
     * Sets the new refilled air amount.
     * 
     * @param refillAirAmount The new value.
     * @see #getRefillAirAmount()
     */
    public void setRefillAirAmount(int refillAirAmount)
    {
        this.refillAirAmount = Math.max(refillAirAmount, 0);
    }
}