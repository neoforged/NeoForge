/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * LivingIncomingDamageEvent is fired when a LivingEntity is about to receive damage.
 * <br>
 * This event is fired in {@link LivingEntity#hurt(DamageSource, float)}
 * after invulnerability checks but before any damage processing/mitigation.
 * <br>
 * For custom posting of this event, the event expects to be fired before any
 * damage reductions have been calculated. This event expects a mutable {@link DamageContainer}.
 * <br>
 * This event is fired via the {@link CommonHooks#onEntityIncomingDamage(LivingEntity, DamageContainer)}.<br>
 * <br>
 * For more information on the damage sequence
 * 
 * @see DamageSequenceEvent
 **/
public class LivingIncomingDamageEvent extends DamageSequenceEvent implements ICancellableEvent {
    public LivingIncomingDamageEvent(LivingEntity entity, DamageContainer container) {
        super(entity, container);
    }

    public DamageSource getSource() {
        return this.container.getSource();
    }

    public float getAmount() {
        return this.container.getNewDamage();
    }
}
