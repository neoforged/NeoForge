/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount.Formula;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired before setting the drop count of an {@link ItemStack} from the {@link ApplyBonusCount} loot function.
 * <p>
 * It allows enchantment levels to be modified, or the drop count to be set to a specific value.
 */
public class BonusLootDropsEvent extends Event {
    private final ItemStack itemStack;
    private final LootContext context;
    private final Enchantment enchantment;
    private final Formula formula;
    private int enchantmentLevel;
    private final int originalDropCount;
    private int newDropCount = -1;

    public BonusLootDropsEvent(ItemStack itemStack, LootContext context, Enchantment enchantment, Formula formula, int enchantmentLevel, int originalDropCount) {
        this.itemStack = itemStack;
        this.context = context;
        this.enchantment = enchantment;
        this.formula = formula;
        this.enchantmentLevel = enchantmentLevel;
        this.originalDropCount = originalDropCount;
    }

    /**
     * Get the {@link ItemStack} for this event.
     *
     * @return the item stack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Get the {@link LootContext} for this event.
     *
     * @return the loot context
     */
    public LootContext getContext() {
        return context;
    }

    /**
     * Get the {@link Enchantment} which caused the loot function to apply.
     *
     * @return the enchantment
     */
    public Enchantment getEnchantment() {
        return enchantment;
    }

    /**
     * Get the {@link Formula} used to calculate the new drop count.
     *
     * @return the formula
     */
    public Formula getFormula() {
        return formula;
    }

    /**
     * Get the current enchantment level for this event.
     *
     * @return the current enchantment level
     */
    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    /**
     * Set the new enchantment level for this event.
     *
     * @param newEnchantmentLevel the new enchantment level
     */
    public void setEnchantmentLevel(int newEnchantmentLevel) {
        this.enchantmentLevel = newEnchantmentLevel;
    }

    /**
     * Get the original drop count as initially calculated by the loot function.
     *
     * @return the original drop count
     */
    public int getOriginalDropCount() {
        return originalDropCount;
    }

    /**
     * Set the new drop count for this event. It will be used as the final drop count.
     *
     * @param newDropCount the new drop count
     */
    public void setDropCount(int newDropCount) {
        this.newDropCount = newDropCount;
    }

    /**
     * Calculate the final drop count for this event. If {@link #newDropCount} has been set, no randomness will be applied using the formula.
     *
     * @return the final drop count
     */
    public int getDropCount() {
        if (newDropCount > -1) {
            return newDropCount;
        } else {
            return formula.calculateNewCount(context.getRandom(), itemStack.getCount(), enchantmentLevel);
        }
    }

    /**
     * Get the entity that caused the loot function to apply, if any.
     *
     * @return the entity, or null if not applicable
     */
    @Nullable
    public Entity getEntity() {
        return context.getParamOrNull(LootContextParams.THIS_ENTITY);
    }
}
