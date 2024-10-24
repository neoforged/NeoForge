/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Exposed as a CAPABILITY by items that need additional granularity on the slots they can be inserted in,
 * and optionally to provide custom processing for insertion, ticking, etc.
 */
public interface IExtensionSlotItem {
    /**
     * Runs once per tick for as long as the item remains equipped in the given slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onWornTick(ItemStack stack, IExtensionSlot slot) {
    }

    /**
     * Called when the item is equipped to an extension slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onEquipped(ItemStack stack, IExtensionSlot slot) {
    }

    /**
     * Called when the item is removed from an extension slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default void onUnequipped(ItemStack stack, IExtensionSlot slot) {
    }

    /**
     * Queries wether or not the stack can be placed in the slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default boolean canEquip(ItemStack stack, IExtensionSlot slot) {
        return true;
    }

    /**
     * Queries wether or not the stack can be removed from the slot.
     *
     * @param stack The ItemStack in the slot.
     * @param slot  The slot being referenced.
     */
    default boolean canUnequip(ItemStack stack, IExtensionSlot slot) {
        return !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }
}
