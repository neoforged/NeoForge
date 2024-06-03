/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.damagesource;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityInvulnerablityCheckEvent;
import net.neoforged.neoforge.event.entity.living.DamageBlockEvent;
import net.neoforged.neoforge.event.entity.living.DamageTakenEvent;
import net.neoforged.neoforge.event.entity.living.EntityPreDamageEvent;
import net.neoforged.neoforge.event.entity.living.IncomingDamageEvent;

/**
 * DamageContainer encapsulates aspects of the entity damage sequence so that
 * relevant context related to damage dealt is accessible throughout the entire
 * sequence.
 * <h4>Note: certain values will be defaults until the stage in the sequence when they are set.</h4>
 * <H3>Damage Sequence and uses</H3><ol>
 * <li>Entity is hurt by a damage source</li>
 * <li>{@link EntityInvulnerablityCheckEvent EntityInvulnerablityCheckEvent}
 * fires and determines if the sequence can commence</li>
 * <li>{@link EntityPreDamageEvent EntityPreDamageEvent} fires
 * and gives access to this. Modifiers should be added here.</li>
 * <li>{@link DamageBlockEvent} fires</li>
 * <li>armor, enchantments, mob effect, and absorption modifiers are applied to the damage</li>
 * <li>{@link IncomingDamageEvent IncomingDamageEvent} fires and
 * provides final values for the preceding modifiers and the last chance to negate the damage but will not
 * undo the preceding effects</li>
 * <li>{@link DamageTakenEvent DamageTakenEvent} fires and provides
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
        ENCHANTMENTS,
        /** Damage reduced from active mob effects */
        MOB_EFFECTS,
        /** Damage absorbed by absorption. */
        ABSORPTION
    }

    /**
     * @return the value passed into {@link LivingEntity#hurt(DamageSource, float)} before
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
     * <h4>Note: only the {@link EntityPreDamageEvent EntityPreDamageEvent}
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
     * @return The damage blocked during the {@link DamageBlockEvent}
     */
    float getBlockedDamage();

    /**
     * @return The durability applied to the applicable shield after {@link DamageBlockEvent}
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
     * This provides a post-reduction value for the reduction and modifiers. This will always return zero
     * before {@link IncomingDamageEvent} and will consume all
     * modifiers prior to the event.
     *
     * @param type the specific source type of the damage reduction
     * @return The amount of damage reduced by armor after vanilla armor reductions and added modifiers
     */
    float getReduction(Reduction type);

    public record ResultDamageContainer(
            float getOriginalDamage,
            DamageSource getSource,
            float getNewDamage,
            float getBlockedDamage,
            float getShieldDamage,
            int getPostAttackInvulnerabilityTicks,
            EnumMap<Reduction, Float> reduction

    ) implements DamageContainer {
        public ResultDamageContainer(DamageContainer container) {
            this(container.getOriginalDamage(), container.getSource(), container.getNewDamage(),
                    container.getBlockedDamage(), container.getShieldDamage(), container.getPostAttackInvulnerabilityTicks(),
                    new EnumMap<Reduction, Float>(Arrays.stream(Reduction.values())
                            .map(type -> new AbstractMap.SimpleEntry<>(type, container.getReduction(type)))
                            .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))));
        }

        @Override
        public void addModifier(Reduction type, BiFunction<DamageContainer, Float, Float> function) {}

        @Override
        public void setNewDamage(float damage) {}

        @Override
        public void setPostAttackInvulnerabilityTicks(int ticks) {}

        @Override
        public float getReduction(Reduction type) {
            return reduction().getOrDefault(type, 0f);
        }
    }
}
