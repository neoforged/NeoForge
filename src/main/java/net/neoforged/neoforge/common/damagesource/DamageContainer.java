/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.damagesource;

import net.minecraft.world.damagesource.DamageSource;
import net.neoforged.neoforge.event.entity.living.DamageBlockEvent;

/**
 * DamageContainer encapsulates aspects of the entity damage sequence so that
 * all context related to damage dealt is accessible throughout the entire
 * sequence.
 * <h4>Note: certain values will be defaults until the stage in the sequence when they are set.</h4>
 * <ul>
 * <li>{@link #originalDamage} stores a reference to the raw damage value passed to the defender from the attacker.</li>
 * <li>{@link #newDamage} represents the event-modified damage to be applied throughout the sequence.</li>
 * <li>{@link #source} an immutable reference to the damage source</li>
 * <li>{@link #armorReduction} the amount reduced by armor.</li>
 * <li>{@link #absorption} the amount of absorption consumed by the damage sequence.</li>
 * <li>{@link #enchantReduction} the amount of damage reduced by enchantments</li>
 * <li>{@link #mobEffectReduction} the amount of damage reduced by mob effects</li>
 * <li>{@link #invulnerabilityTicksAfterAttack} defaults to 20. Changes how long an entity is invulnerable after this damage sequence.
 * This can be modified at any point.</li>
 * <li>{@link #blockedDamage} The amount of damage blocked by the shield. This value is set by {@link DamageBlockEvent} and cannot
 * be directly modified. {@link #getBlockedDamage()} can be used to obtain the value after the event is fired.</li>
 * <li>{@link #shieldDamage} How much damage the shield item will take.</li>
 * </ul><br>
 */
public class DamageContainer {
    private final float originalDamage;
    private final DamageSource source;
    private float newDamage;
    private float armorReduction = 0f;
    private float absorption = 0f;
    private float enchantReduction = 0f;
    private float mobEffectReduction = 0f;
    private float blockedDamage = 0;
    private float shieldDamage = 0;
    private int invulnerabilityTicksAfterAttack = 20;

    public DamageContainer(DamageSource source, float originalDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.newDamage = originalDamage;
    }

    public float getOriginalDamage() {
        return originalDamage;
    }

    public DamageSource getSource() {
        return source;
    }

    public DamageBlockEvent setBlockedDamage(DamageBlockEvent event) {
        this.blockedDamage = event.getBlockedDamage();
        this.shieldDamage = event.shieldDamage();
        return event;
    }

    public float getBlockedDamage() {
        return blockedDamage;
    }

    public float getShieldDamage() {
        return shieldDamage;
    }

    public void setPostAttackInvulnerabilityTicks(int ticks) {
        this.invulnerabilityTicksAfterAttack = ticks;
    }

    public int getPostAttackInvulnerabilityTicks() {
        return invulnerabilityTicksAfterAttack;
    }

    public void setArmorReduction(float reduction) {
        this.armorReduction = reduction;
    }

    public float getArmorReduction() {
        return armorReduction;
    }

    public void setEnchantReduction(float reduction) {
        this.enchantReduction = reduction;
    }

    public float getEnchantReduction() {
        return enchantReduction;
    }

    public void setMobEffectReduction(float reduction) {
        this.mobEffectReduction = reduction;
    }

    public float getMobEffectReduction() {
        return mobEffectReduction;
    }

    public void setAbsorption(float absorption) {
        this.absorption = absorption;
    }

    public float getAbsorption() {
        return absorption;
    }

    public void setNewDamage(float damage) {
        this.newDamage = damage;
    }

    public float getNewDamage() {
        return newDamage;
    }
}
