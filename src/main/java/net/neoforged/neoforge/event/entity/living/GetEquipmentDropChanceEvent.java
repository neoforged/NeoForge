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

/// Fired on the server when an entity is dying to determine the chance of dropping worn equipment after enchantments process [EnchantmentEffectComponents#EQUIPMENT_DROPS].
///
/// The entity that was killed and is dropping equipment is accessible in [#getEntity()]. Use [#getKillingBlow()] for the killing entity.
///
/// Only fired if the initial drop chance was greater than 0, meaning it will not fire for cosmetic equipment such as pumpkins during Halloween.
///
/// Will be fired for equipment that was picked up by the entity with a guaranteed drop, use [#isPreserved()] to check for that.
///
/// If your goal is to implement looting like behavior from an item stack used in the main hand, and you do not need the additional context, prefer [GetEnchantmentLevelEvent] for managing enchantment levels.
public class GetEquipmentDropChanceEvent extends LivingEvent {
    private final ServerLevel level;
    private final DamageSource killingBlow;
    private float chance;
    private final float originalChance;
    private final EquipmentSlot slot;
    private boolean canDrop;
    private boolean preserved;

    public GetEquipmentDropChanceEvent(ServerLevel level, LivingEntity entity, DamageSource killingBlow, float chance, float originalChance, EquipmentSlot slot, boolean killedByPlayer, boolean preserved) {
        super(entity);
        this.level = level;
        this.killingBlow = killingBlow;
        this.chance = chance;
        this.originalChance = originalChance;
        this.slot = slot;
        this.canDrop = killedByPlayer || preserved;
        this.preserved = preserved;
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

    /// {@return true if the equipment can drop} Will by default be true if the equipment was picked up or the mob was killed by a player.
    public boolean getCanDrop() {
        return canDrop;
    }

    /// Sets whether the item is allowed to drop. Note that it may still fail to drop if [#getChance()] is less than 1.
    ///
    /// @param canDrop If true, the item
    public void setCanDrop(boolean canDrop) {
        this.canDrop = canDrop;
    }

    /// {@return true if the equipment will not be damaged when dropped} Typically means the item was picked rather than natural equipment.
    public boolean isPreserved() {
        return preserved;
    }

    /// Sets whether the item should be damaged if dropped.
    /// 
    /// This does not change whether the item can drop, for that use [#setCanDrop(boolean)].
    ///
    /// @param preserved If true, the item durability will not be changed when dropped.
    public void setPreserved(boolean preserved) {
        this.preserved = preserved;
    }

    /// {@return the current chance modified by enchantments and event consumers}
    ///
    /// @see #getOriginalChance()
    public float getChance() {
        return chance;
    }

    /// Sets the chance for this equipment to drop as a value between 0 and 1. 0 or fewer makes it never drop. 1 or more makes it guaranteed to drop.
    ///
    /// @param chance New chance
    public void setChance(float chance) {
        this.chance = chance;
    }

    /// {@return the slot containing the item to drop}
    public EquipmentSlot getSlot() {
        return slot;
    }

    /// {@return the stack being considered for dropping}
    public ItemStack getStack() {
        return getEntity().getItemBySlot(slot);
    }
}
