/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * DamageTakenEvent is fired just before health is modified on the entity.<br>
 * At this point armor, potion and absorption modifiers have already been applied to damage.
 * the {@link DamageContainer} is immutable and represents a FINAL value of what is about to
 * be applied.
 * <br>
 * Also note that appropriate resources (like armor durability and absorption extra hearts) have already been consumed.<br>
 * This event is fired whenever an Entity is damaged in {@code LivingEntity#actuallyHurt(DamageSource, float)} and
 * {@code Player#actuallyHurt(DamageSource, float)}.<br>
 * <br>
 * This event is fired via {@link CommonHooks#onLivingDamageTaken(LivingEntity, DamageContainer)}.<br>
 * <br>
 * 
 * @see DamageSequenceEvent
 **/
public class DamageTakenEvent extends DamageSequenceEvent {
    public DamageTakenEvent(LivingEntity entity, DamageContainer container) {
        super(entity, container);
    }
}
