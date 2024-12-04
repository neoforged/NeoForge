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
import net.neoforged.neoforge.common.damagesource.IReductionFunction;

/**
 * LivingIncomingDamageEvent is fired when a LivingEntity is about to receive damage.
 * <br>
 * This event is fired in {@link LivingEntity#hurt(DamageSource, float)}
 * after invulnerability checks but before any damage processing/mitigation.
 * <br>
 * For custom posting of this event, the event expects to be fired before any
 * damage reductions have been calculated. This event expects a mutable {@link DamageContainer}.
 * <br>
 * This event is fired via the {@link CommonHooks#onEntityIncomingDamage(LivingEntity, DamageContainer)}.
 * 
 * @see DamageContainer for more information on the damage sequence
 **/
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent {
    private final DamageContainer container;

    public LivingIncomingDamageEvent(LivingEntity entity, DamageContainer container) {
        super(entity);
        this.container = container;
    }

    /** {@return the container for this damage sequence} */
    public DamageContainer getContainer() {
        return this.container;
    }

    /** {@return the {@link DamageSource} for this damage sequence} */
    public DamageSource getSource() {
        return this.container.getSource();
    }

    /** {@return the current damage to be applied to the entity} */
    public float getAmount() {
        return this.container.getNewDamage();
    }

    /** {@return the damage value passed into the damage sequence before modifications} */
    public float getOriginalAmount() {
        return this.container.getOriginalDamage();
    }

    /**
     * @param newDamage the damage value to be used in the rest of the damage sequence.
     */
    public void setAmount(float newDamage) {
        this.container.setNewDamage(newDamage);
    }

    /**
     * Reduction modifiers alter the vanilla damage reduction before it modifies the damage value.
     * Modifiers are executed in sequence.
     *
     * @param type          the reduction type to be modified
     * @param reductionFunc the function to apply to the reduction value.
     */
    public void addReductionModifier(DamageContainer.Reduction type, IReductionFunction reductionFunc) {
        this.container.addModifier(type, reductionFunc);
    }

    /**
     * When an entity's invulnerable time is fully cooled down, 20 ticks of invulnerability is added
     * on the next attack. This method allows for setting a new invulnerability tick count when those
     * conditions are met.
     * <br>
     * <i>Note: this value will be ignored if the damage is taken while invulnerability ticks are greater
     * than 10 and the damage source does not bypass invulnerability</i>
     * 
     * @param ticks the number of ticks for the entity to remain invulnerable to incoming damage
     */
    public void setInvulnerabilityTicks(int ticks) {
        this.container.setPostAttackInvulnerabilityTicks(ticks);
    }
}
