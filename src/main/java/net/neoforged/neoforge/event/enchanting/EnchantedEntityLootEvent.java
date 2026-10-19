/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.enchanting;

import com.google.common.base.Preconditions;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.neoforged.neoforge.event.entity.living.GetEquipmentDropChanceEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fired on the server when an entity is dropping loot and the level of a specific enchantment is being queried.
///
/// Notably, this fires when [EnchantedCountIncreaseFunction] or [LootItemRandomChanceWithEnchantedBonusCondition] are evaluated.
/// Mods that implement similar loot conditions or functions should fire this event as well.
///
/// This event does not affect the drop chances of equipment. If you need to change that, use [GetEquipmentDropChanceEvent]
///
/// If you do not need the additional context of this event, prefer [GetEnchantmentLevelEvent] for managing enchantment levels.
public class EnchantedEntityLootEvent extends LivingEvent {
    private final DamageSource damageSource;
    private final Holder<Enchantment> enchantment;
    private int enchantmentLevel;
    private final @Nullable Entity target;

    @ApiStatus.Internal
    public EnchantedEntityLootEvent(LivingEntity entity, DamageSource damageSource, Holder<Enchantment> enchantment, int enchantmentLevel, @Nullable Entity target) {
        super(entity);
        this.damageSource = damageSource;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
        this.target = target;
    }

    /// Returns the attacker entity, which was the source of the enchantment.
    ///
    /// To get the target entity, use [#getTarget()]
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

    /// Returns the entity that is dropping loot from [LootContextParams#THIS_ENTITY].
    /// * For [LootContextParamSets#ENTITY], will be the entity killed.
    /// * For [LootContextParamSets#FISHING], will be the [FishingHook], though may be null when fishing by other means.
    ///
    /// To get the attacking entity, use [#getEntity()].
    ///
    /// @return the entity that is dropping loot.
    @Nullable
    public Entity getTarget() {
        return this.target;
    }
}
