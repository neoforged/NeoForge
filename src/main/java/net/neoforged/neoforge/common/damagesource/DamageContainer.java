/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.damagesource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * DamageContainer encapsulates aspects of the entity damage sequence so that
 * relevant context related to damage dealt is accessible throughout the entire
 * sequence.
 * <p>Note: certain values will be defaults until the stage in the sequence when they are set.</p>
 * <h3>The Damage Sequence</h3>
 * <ol>
 * <li>{@link LivingEntity#hurt} is invoked on the recipient from the source of
 * the attack.</li>
 * <li>{@link Entity#isInvulnerableTo} is invoked and fires {@link EntityInvulnerabilityCheckEvent}.</li>
 * <li>After determining the entity is vulnerable, the {@link DamageContainer} in instantiated for the entity.</li>
 * <li>{@link LivingIncomingDamageEvent} is fired.</li>
 * <li>{@link LivingShieldBlockEvent} fires and the result determines if shield effects apply.</li>
 * <li>{@link LivingEntity#actuallyHurt} is called.</li>
 * <li>armor, magic, mob_effect, and absorption reductions are captured in the DamageContainer.</li>
 * <li>{@link LivingDamageEvent.Pre} is fired.</li>
 * <li>if the damage is not zero, entity health is modified and recorded and {@link LivingDamageEvent.Post} is fired.</li>
 * </ol>
 */
@ApiStatus.Internal
public class DamageContainer {
    public enum Reduction {
        /** Damage reduced from the effects of armor. */
        ARMOR,
        /** Damage reduced from enchantments on armor. */
        ENCHANTMENTS,
        /** Damage reduced from active mob effects. */
        MOB_EFFECTS,
        /** Damage absorbed by absorption. */
        ABSORPTION
    }

    private final EnumMap<Reduction, List<IReductionFunction>> reductionFunctions = new EnumMap<>(Reduction.class);
    private final float originalDamage;
    private final DamageSource source;
    private float newDamage;
    private final EnumMap<Reduction, Float> reductions = new EnumMap<>(Reduction.class);
    private float blockedDamage = 0f;
    private float shieldDamage = 0;
    private int invulnerabilityTicksAfterAttack = 20;

    public DamageContainer(DamageSource source, float originalDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.newDamage = originalDamage;
    }

    /** {@return the value passed into {@link LivingEntity#hurt(DamageSource, float)} before any modifications are made} */
    public float getOriginalDamage() {
        return originalDamage;
    }

    /** {@return the damage source for this damage sequence} */
    public DamageSource getSource() {
        return source;
    }

    /**
     * This sets the current damage value for the entity at the stage of the damage sequence in which it is set.
     * Subsequent steps in the damage sequence will use and modify this value accordingly. If this is called in
     * the final step of the sequence, this value will be applied against the entity's health.
     *
     * @param damage the amount to harm this entity at the end of the damage sequence
     */
    public void setNewDamage(float damage) {
        this.newDamage = damage;
    }

    /** {@return the current amount expected to be applied to the entity or used in subsequent damage calculations} */
    public float getNewDamage() {
        return newDamage;
    }

    /**
     * Adds a callback modifier to the vanilla damage reductions. Each function will be performed in sequence
     * on the vanilla value at the time the {@link DamageContainer.Reduction} type is set by vanilla.
     * <h4>Note: only the {@link LivingIncomingDamageEvent EntityPreDamageEvent}
     * happens early enough in the sequence for this method to have any effect.</h4>
     *
     * @param type              The reduction type your function will apply to
     * @param reductionFunction takes the current reduction from vanilla and any preceding functions and returns a new
     *                          value for the reduction. These are always executed in insertion order. if sequence
     *                          matters, use {@link net.neoforged.bus.api.EventPriority} to order your function.
     */

    public void addModifier(Reduction type, IReductionFunction reductionFunction) {
        this.reductionFunctions.computeIfAbsent(type, a -> new ArrayList<>()).add(reductionFunction);
    }

    /** {@return the damage blocked during the {@link LivingShieldBlockEvent}} */
    public float getBlockedDamage() {
        return blockedDamage;
    }

    /** {@return the durability applied to the applicable shield after {@link LivingShieldBlockEvent} returned a successful block} */
    public float getShieldDamage() {
        return shieldDamage;
    }

    /**
     * Explicitly sets the invulnerability ticks after the damage has been applied.
     *
     * @param ticks Ticks of invulnerability after this damage sequence
     */
    public void setPostAttackInvulnerabilityTicks(int ticks) {
        this.invulnerabilityTicksAfterAttack = ticks;
    }

    /** {@return the number of ticks this entity will be invulnerable after damage is applied} */
    public int getPostAttackInvulnerabilityTicks() {
        return invulnerabilityTicksAfterAttack;
    }

    /**
     * This provides a post-reduction value for the reduction and modifiers. This will always return zero
     * before {@link LivingDamageEvent.Pre} and will consume all
     * modifiers prior to the event.
     *
     * @param type the specific source type of the damage reduction
     * @return The amount of damage reduced by armor after vanilla armor reductions and added modifiers
     */
    public float getReduction(Reduction type) {
        return reductions.getOrDefault(type, 0f);
    }

    //=============INTERNAL METHODS - DO NOT USE===================

    @ApiStatus.Internal
    public void setBlockedDamage(LivingShieldBlockEvent event) {
        if (event.getBlocked()) {
            this.blockedDamage = event.getBlockedDamage();
            this.shieldDamage = event.shieldDamage();
            this.newDamage -= this.blockedDamage;
        }
    }

    @ApiStatus.Internal
    public void setReduction(Reduction reduction, float amount) {
        this.reductions.put(reduction, modifyReduction(Reduction.ABSORPTION, amount));
        this.newDamage -= Math.max(0, amount);
    }

    private float modifyReduction(Reduction type, float reduction) {
        for (var func : reductionFunctions.getOrDefault(type, new ArrayList<>())) {
            reduction = func.modify(this, reduction);
        }
        return reduction;
    }
}
