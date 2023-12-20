/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * LivingDamageEvent is fired just before damage is applied to entity.<br>
 * At this point armor, potion and absorption modifiers have already been applied to damage - this is FINAL value.<br>
 * Also note that appropriate resources (like armor durability and absorption extra hearths) have already been consumed.<br>
 * This event is fired whenever an Entity is damaged in
 * {@code LivingEntity#actuallyHurt(DamageSource, float)} and
 * {@code Player#actuallyHurt(DamageSource, float)}.<br>
 * <br>
 * This event is fired via the {@link CommonHooks#onLivingDamage(LivingEntity, DamageSource, float)}.<br>
 * <br>
 * {@link #source} contains the DamageSource that caused this Entity to be hurt. <br>
 * {@link #amount} contains the final amount of damage that will be dealt to entity. <br>
 * <br>
 * This event is {@link ICancellableEvent}.<br>
 * If this event is canceled, the Entity is not hurt. Used resources WILL NOT be restored.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * 
 * @see LivingHurtEvent
 **/
public class LivingDamageEvent extends LivingEvent implements ICancellableEvent {
    private final DamageSource source;
    private float amount;

    public LivingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
