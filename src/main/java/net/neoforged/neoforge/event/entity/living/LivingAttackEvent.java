/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;

/**
 * LivingAttackEvent is fired when a living Entity is attacked. <br>
 * This event is fired whenever an Entity is attacked in
 * {@link LivingEntity#hurt(DamageSource, float)} and
 * {@link Player#hurt(DamageSource, float)}. <br>
 * <br>
 * This event is fired via the {@link CommonHooks#onLivingAttack(LivingEntity, DamageSource, float)}.<br>
 * <br>
 * {@link #source} contains the DamageSource of the attack. <br>
 * {@link #amount} contains the amount of damage dealt to the entity. <br>
 * <br>
 * This event is {@link net.neoforged.bus.api.ICancellableEvent}.<br>
 * If this event is canceled, the Entity does not take attack damage.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 **/
public class LivingAttackEvent extends LivingEvent implements ICancellableEvent {
    private final DamageSource source;
    private final float amount;

    public LivingAttackEvent(LivingEntity entity, DamageSource source, float amount) {
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
}
