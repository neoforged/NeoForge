/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * LivingDamageEvent captures when an entity is about to receive a loss
 * of health. At this stage in the damage sequence, all reduction effects
 * have been applied.
 * <br>
 * {@link Pre} allows for modification of the damage value before it is applied
 * to the entity's health.
 * <br>
 * {@link Post} contains an immutable representation of the entire damage sequence
 * and allows for reference to the values accrued at each step.
 * <br>
 * For more information on the damage sequence
 * 
 * @see DamageSequenceEvent
 */
public abstract class LivingDamageEvent extends DamageSequenceEvent {
    private LivingDamageEvent(LivingEntity entity, DamageContainer container) {
        super(entity, container);
    }

    /**
     * LivingDamageEvent.Pre is fired when an Entity is set to be hurt. <br>
     * At this point armor, potion and absorption modifiers have already been applied to the damage value
     * and the entity.<br>
     * This event is fired in {@code LivingEntity#actuallyHurt(DamageSource, float}
     * <br>
     * For custom posting of this event, the event expects to be fired after
     * damage reductions have been calculated but before any changes to the entity
     * health has been applied. This event expects a mutable {@link DamageContainer}.
     * <br>
     * This event is fired via the {@link CommonHooks#onLivingDamagePre(LivingEntity, DamageContainer)}.
     * <br>
     * For more information on the damage sequence
     * 
     * @see DamageSequenceEvent
     **/
    public static class Pre extends LivingDamageEvent {
        public Pre(LivingEntity entity, DamageContainer source) {
            super(entity, source);
        }
    }

    /**
     * LivingDamageEvent.Post is fired after health is modified on the entity.<br>
     * the {@link DamageContainer} is immutable and represents a FINAL value of what was applied to the entity.
     * <br>
     * Also note that appropriate resources (like armor durability and absorption extra hearts) have already been consumed.<br>
     * This event is fired whenever an Entity is damaged in {@code LivingEntity#actuallyHurt(DamageSource, float)}
     * <br>
     * This event is fired via {@link CommonHooks#onLivingDamagePost(LivingEntity, DamageContainer)}.<br>
     * <br>
     * For more information on the damage sequence
     * 
     * @see DamageSequenceEvent
     **/
    public static class Post extends LivingDamageEvent {
        public Post(LivingEntity entity, DamageContainer container) {
            super(entity, container);
        }
    }
}
