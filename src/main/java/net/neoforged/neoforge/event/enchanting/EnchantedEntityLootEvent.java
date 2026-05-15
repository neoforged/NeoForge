/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.enchanting;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Fired on the server when an entity is dropping loot and the level of a specific enchantment is being queried.
 * <p>
 * Notably, this fires when {@link EnchantedCountIncreaseFunction} or {@link LootItemRandomChanceWithEnchantedBonusCondition} are evaluated,
 * as well as when {@link EnchantmentEffectComponent#EQUIPMENT_DROPS} is being processed.
 * Mods that implement similar loot conditions or functions should fire this event as well.
 * <p>
 * If you do not need the additional context of this event, prefer {@link GetEnchantmentLevelEvent} for managing enchantment levels.
 */
public class EnchantedEntityLootEvent extends LivingEvent {
    @Nullable
    private final DamageSource damageSource;
    private final Holder<Enchantment> enchantment;
    private int enchantmentLevel;

    @ApiStatus.Internal
    public EnchantedEntityLootEvent(LivingEntity entity, DamageSource damageSource, Holder<Enchantment> enchantment, int enchantmentLevel) {
        super(entity);
        this.damageSource = damageSource;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    /**
     * Returns the entity that is dropping loot. This is generally the entity that was killed.
     * <p>
     * To get the attacking entity, use the {@link #getDamageSource() damage source}.
     */
    @Override
    public LivingEntity getEntity() {
        return super.getEntity();
    }

    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    public int getEnchantmentLevel() {
        return this.enchantmentLevel;
    }

    /**
     * Sets the new enchantment level.
     * 
     * @throws IllegalArgumentException if the enchantment level is negative.
     */
    public void setEnchantmentLevel(int enchantmentLevel) {
        Preconditions.checkArgument(enchantmentLevel >= 0, "Enchantment level cannot be negative");
        this.enchantmentLevel = enchantmentLevel;
    }
}
