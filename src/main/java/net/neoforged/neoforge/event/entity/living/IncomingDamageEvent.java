/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * IncomingDamageEvent is fired when an Entity is set to be hurt. <br>
 * This event is fired in {@code LivingEntity#hurt(DamageSource, float} and
 * {@code Player#actuallyHurt(DamageSource, float)}.
 * <br>
 * For custom posting of this event, the event expects to be fired after
 * damage reductions have been calculated but before any changes to the entity
 * health has been applied. This event expects a mutable {@link DamageContainer}.
 * <br>
 * This event is fired via the {@link CommonHooks#onIncomingDamage(LivingEntity, DamageContainer)}.
 * <br>
 * 
 * @see DamageSequenceEvent
 **/
public class IncomingDamageEvent extends DamageSequenceEvent {
    public IncomingDamageEvent(LivingEntity entity, DamageContainer source) {
        super(entity, source);
    }
}
