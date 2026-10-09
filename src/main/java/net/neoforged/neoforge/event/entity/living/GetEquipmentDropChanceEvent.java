/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fired on the server when an entity is dying to determine the chance of dropping worn equipment after enchantments process {@link EnchantmentEffectComponents#EQUIPMENT_DROPS}.
///
/// The entity that was killed and is dropping equipment is accessible in {@link #getEntity()}. Use {@link #getKillingBlow()} for the killing entity.
///
/// Only fired if the initial drop chance was greater than 0, meaning it will not fire for cosmetic equipment such as pumpkins during Halloween.
///
/// Will be fired for equipment that was picked up by the entity with a guaranteed drop, use {@link #isPreserved()} to check for that.
///
/// If your goal is to implement looting like behavior from an item stack used in the main hand, and you do not need the additional context, prefer {@link GetEnchantmentLevelEvent} for managing enchantment levels.
public class GetEquipmentDropChanceEvent extends LivingEvent {
    private final ServerLevel level;
    private final DamageSource killingBlow;
    private final MutableFloat chance;
    private final float originalChance;
    @Nullable
    private final EquipmentSlot slot;

    @ApiStatus.Internal
    public GetEquipmentDropChanceEvent(ServerLevel level, LivingEntity entity, DamageSource killingBlow, MutableFloat chance, float originalChance, @Nullable EquipmentSlot slot) {
        super(entity);
        this.level = level;
        this.killingBlow = killingBlow;
        this.chance = chance;
        this.originalChance = originalChance;
        this.slot = slot;
    }

    public ServerLevel getLevel() {
        return level;
    }

    /// {@return the damage source used to kill the entity} Will contain the killing entity if present.
    public DamageSource getKillingBlow() {
        return killingBlow;
    }

    /// {@return the chance before any enchantments or event consumers ran}
    ///
    /// @see #getChance()
    public float getOriginalChance() {
        return originalChance;
    }

    /// {@return true if the equipment was guaranteed to drop. Typically means the item was picked rather than natural equipment}
    public boolean isPreserved() {
        return originalChance > 1.0f;
    }

    /// {@return the current chance modified by enchantments and event consumers}
    ///
    /// @see #getOriginalChance()
    public float getChance() {
        return chance.floatValue();
    }

    /// Sets the chance for this equipment to drop as a value between 0 and 1. 0 or fewer makes it never drop. 1 or more makes it guaranteed to drop.
    ///
    /// @param chance New chance
    public void setChance(float chance) {
        this.chance.setValue(chance);
    }

    /// {@return the slot containing the item to drop} May be null if {@link net.minecraft.world.item.enchantment.EnchantmentHelper#processEquipmentDropChance(ServerLevel, LivingEntity, DamageSource, float, EquipmentSlot)} is called without the slot.
    public @Nullable EquipmentSlot getSlot() {
        return slot;
    }

    /// {@return the stack being considered for dropping} May be empty if {@link net.minecraft.world.item.enchantment.EnchantmentHelper#processEquipmentDropChance(ServerLevel, LivingEntity, DamageSource, float, EquipmentSlot)} is called without the slot.
    public ItemStack getStack() {
        return slot == null ? ItemStack.EMPTY : getEntity().getItemBySlot(slot);
    }
}
