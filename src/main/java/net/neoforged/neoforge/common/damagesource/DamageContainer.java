/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.damagesource;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.DamageBlockEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * DamageContainer encapsulates aspects of the entity damage sequence so that
 * relevant context related to damage dealt is accessible throughout the entire
 * sequence.
 * <h4>Note: certain values will be defaults until the stage in the sequence when they are set.</h4>
 * <H3>Damage Sequence and uses</H3><ol>
 * <li>Entity is hurt by a damage source</li>
 * <li>{@link net.neoforged.neoforge.event.entity.EntityInvulnerablityCheckEvent EntityInvulnerablityCheckEvent}
 * fires and determines if the sequence can commence</li>
 * <li>{@link net.neoforged.neoforge.event.entity.living.EntityPreDamageEvent EntityPreDamageEvent} fires
 * and gives access to this. Modifiers should be added here.</li>
 * <li>{@link DamageBlockEvent} fires</li>
 * <li>armor, enchantments, mob effect, and absorption modifiers are applied to the damage</li>
 * <li>{@link net.neoforged.neoforge.event.entity.living.IncomingDamageEvent IncomingDamageEvent} fires and
 * provides final values for the preceding modifiers and the last chance to negate the damage but will not
 * undo the preceding effects</li>
 * <li>{@link net.neoforged.neoforge.event.entity.living.DamageTakenEvent DamageTakenEvent} fires and provides
 * an immutable perspective of what the entire sequence ended with. </li>
 * </ol>
 *
 *
 */
public interface DamageContainer {
    public enum Reduction {
        /** Damage reduced from the effects of armor */
        ARMOR,
        /** Damage reduced from enchantments on armor */
        ENCHANT,
        /** Damage reduced from active mob effects */
        MOBEFFECT,
        /** Damage absorbed by absorption. */
        ABSORPTION
    }

    /**
     * @return the value passed into {@link net.minecraft.world.entity.LivingEntity#hurt(DamageSource, float)} before
     *         any modifications are made.
     */
    float getOriginalDamage();

    /**
     * @return the current amount expected to be applied to the entity or used in subsequent damage calculations.
     */
    float getNewDamage();

    /**
     * @return The damage source for this damage sequence
     */
    DamageSource getSource();

    /**
     * Adds a callback modifier to the vanilla damage reductions. Each function will be performed in sequence
     * on the vanilla value at the time the {@link Reduction} type is set by vanilla.
     * <h4>Note: only the {@link net.neoforged.neoforge.event.entity.living.IncomingDamageEvent IncomingDamageEvent}
     * happens early enough in the sequence for this method to have any effect.</h4>
     *
     * @param type      The reduction type your function will apply to
     * @param operation takes the current reduction from vanilla and any preceding functions and returns a new
     *                  value for the reduction. These are always executed in insertion order. if sequence
     *                  matters, use {@link net.neoforged.bus.api.EventPriority} to order your function.
     */
    void addModifier(Reduction type, BiFunction<DamageContainer, Float, Float> operation);

    /**
     * This sets the current damage value for the entity at the stage of the damage sequence in which it is set.
     * Subsequent steps in the damage sequence will use and modify this value accordingly. If this is called in
     * the final step of the sequence, this value will be applied against the entity's health.
     *
     * @param damage the amount to harm this entity at the end of the damage sequence
     */
    void setNewDamage(float damage);

    /**
     * @return The damage blocked during the {@link net.neoforged.neoforge.event.entity.living.DamageBlockEvent}
     */
    float getBlockedDamage();

    /**
     * @return The durability applied to the applicable shield after {@link net.neoforged.neoforge.event.entity.living.DamageBlockEvent}
     *         returned a successful block
     */
    float getShieldDamage();

    /**
     * Explicitly sets the invulnerability ticks after the damage has been applied.
     *
     * @param ticks Ticks of invulnerability after this damage sequence
     */
    void setPostAttackInvulnerabilityTicks(int ticks);

    /**
     * @return The number of ticks this entity will be invulnerable after damage is applied.
     */
    int getPostAttackInvulnerabilityTicks();

    /**
     * This provides a post-reduction value for armor reduction and modifiers. This will always return zero
     * before {@link net.neoforged.neoforge.event.entity.living.IncomingDamageEvent} and will consume all
     * modifiers prior to the event.
     *
     * @return The amount of damage reduced by armor after vanilla armor reductions and added modifiers
     */
    float getArmorReduction();

    /**
     * This provides a post-reduction value for enchantment reduction and modifiers. This will always return zero
     * before {@link net.neoforged.neoforge.event.entity.living.IncomingDamageEvent} and will consume all
     * modifiers prior to the event.
     *
     * @return the amount of damage reduced by enchantments after vanilla enchantment reductions and added modifiers
     */
    float getEnchantReduction();

    /**
     * This provides a post-reduction value for mob effect reduction and modifiers. This will always return zero
     * before {@link net.neoforged.neoforge.event.entity.living.IncomingDamageEvent} and will consume all
     * modifiers prior to the event.
     *
     * @return The amount of damage reduced by mob effects after vanilla mob effect reductions and added modifiers
     */
    float getMobEffectReduction();

    /**
     * This provides a post-reduction value for absorption consumption and modifiers. This will always return zero
     * before {@link net.neoforged.neoforge.event.entity.living.IncomingDamageEvent} and will consume all
     * modifiers prior to the event.
     *
     * @return The amount of absorption consumed after vanilla absorption consumption and added modifiers
     */
    float getAbsorption();

    public class InternalDamageContainer implements DamageContainer {
        private final EnumMap<Reduction, List<BiFunction<DamageContainer, Float, Float>>> reductionMap = new EnumMap<>(Reduction.class);
        private final float originalDamage;
        private final DamageSource source;
        private float newDamage;
        private float armorReduction = 0f;
        private float absorption = 0f;
        private float enchantReduction = 0f;
        private float mobEffectReduction = 0f;
        private float blockedDamage = 0f;
        private float shieldDamage = 0;
        private int invulnerabilityTicksAfterAttack = 20;

        public InternalDamageContainer(DamageSource source, float originalDamage) {
            this.source = source;
            this.originalDamage = originalDamage;
            this.newDamage = originalDamage;
        }

        @Override
        public float getOriginalDamage() {
            return originalDamage;
        }

        @Override
        public DamageSource getSource() {
            return source;
        }

        @Override
        public void setNewDamage(float damage) {
            this.newDamage = damage;
        }

        @Override
        public float getNewDamage() {
            return newDamage;
        }

        public void addModifier(Reduction type, BiFunction<DamageContainer, Float, Float> operation) {
            this.reductionMap.computeIfAbsent(type, a -> new ArrayList<>()).add(operation);
        }

        @Override
        public float getBlockedDamage() {
            return blockedDamage;
        }

        @Override
        public float getShieldDamage() {
            return shieldDamage;
        }

        @Override
        public void setPostAttackInvulnerabilityTicks(int ticks) {
            this.invulnerabilityTicksAfterAttack = ticks;
        }

        @Override
        public int getPostAttackInvulnerabilityTicks() {
            return invulnerabilityTicksAfterAttack;
        }

        @Override
        public float getArmorReduction() {
            return armorReduction;
        }

        @Override
        public float getEnchantReduction() {
            return enchantReduction;
        }

        @Override
        public float getMobEffectReduction() {
            return mobEffectReduction;
        }

        @Override
        public float getAbsorption() {
            return absorption;
        }

        //=============INTERNAL METHODS - DO NOT USE===================

        @ApiStatus.Internal
        public DamageBlockEvent setBlockedDamage(DamageBlockEvent event) {
            if (event.getBlocked()) {
                this.blockedDamage = event.getBlockedDamage();
                this.shieldDamage = event.shieldDamage();
                this.newDamage -= this.blockedDamage;
            }
            return event;
        }

        @ApiStatus.Internal
        public void setAbsorption(float absorption) {
            this.absorption = modifyReduction(Reduction.ABSORPTION, absorption);
            this.newDamage -= Math.max(0, absorption);
        }

        @ApiStatus.Internal
        public void setMobEffectReduction(float reduction) {
            this.mobEffectReduction = modifyReduction(Reduction.MOBEFFECT, reduction);
            this.newDamage -= Math.max(0, reduction);
        }

        @ApiStatus.Internal
        public void setEnchantReduction(float reduction) {
            this.enchantReduction = modifyReduction(Reduction.ENCHANT, reduction);
            this.newDamage -= Math.max(0, reduction);
        }

        @ApiStatus.Internal
        public void setArmorReduction(float reduction) {
            this.armorReduction = modifyReduction(Reduction.ARMOR, reduction);
            this.newDamage -= Math.max(0, this.armorReduction);
        }

        private float modifyReduction(Reduction type, float reduction) {
            for (var func : reductionMap.getOrDefault(type, new ArrayList<>())) {
                reduction = func.apply(this, reduction);
            }
            return reduction;
        }
    }

    public record ResultDamageContainer(
            float getOriginalDamage,
            DamageSource getSource,
            float getNewDamage,
            float getBlockedDamage,
            float getShieldDamage,
            int getPostAttackInvulnerabilityTicks,
            float getArmorReduction,
            float getEnchantReduction,
            float getMobEffectReduction,
            float getAbsorption

    ) implements DamageContainer {
        public ResultDamageContainer(DamageContainer container) {
            this(container.getOriginalDamage(), container.getSource(), container.getNewDamage(),
                    container.getBlockedDamage(), container.getShieldDamage(), container.getPostAttackInvulnerabilityTicks(),
                    container.getArmorReduction(), container.getEnchantReduction(), container.getMobEffectReduction(),
                    container.getAbsorption());
        }

        @Override
        public void addModifier(Reduction type, BiFunction<DamageContainer, Float, Float> function) {}

        @Override
        public void setNewDamage(float damage) {}

        @Override
        public void setPostAttackInvulnerabilityTicks(int ticks) {}
    }
}
