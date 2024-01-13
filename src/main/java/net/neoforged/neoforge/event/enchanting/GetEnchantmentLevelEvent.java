/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.enchanting;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;

/**
 * This event is fired whenever the enchantment level of a particular item is requested for gameplay purposes.<br>
 * It is called from {@link IItemStackExtension#getEnchantmentLevel(Enchantment)} and {@link IItemStackExtension#getAllEnchantments()}.
 * <p>
 * It is not fired for interactions with NBT, which means these changes will not reflect in the item tooltip.
 */
public class GetEnchantmentLevelEvent extends Event {

    protected final ItemStack stack;
    protected final Map<Enchantment, Integer> enchantments;
    @Nullable
    protected final Enchantment targetEnchant;

    public GetEnchantmentLevelEvent(ItemStack stack, Map<Enchantment, Integer> enchantments, @Nullable Enchantment targetEnchant) {
        this.stack = stack;
        this.enchantments = enchantments;
        this.targetEnchant = targetEnchant;
    }

    /**
     * Returns the item stack that is being queried against.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Returns the mutable enchantment->level map.
     */
    public Map<Enchantment, Integer> getEnchantments() {
        return this.enchantments;
    }

    /**
     * This method returns the specific enchantment being queried from {@link IItemStackExtension#getEnchantmentLevel(Enchantment)}.
     * <p>
     * If this is value is present, you only need to adjust the level of that enchantment.
     * <p>
     * If this value is null, then the event was fired from {@link IItemStackExtension#getAllEnchantments()} and all enchantments should be populated.
     * 
     * @return The specific enchantment being queried, or null, if all enchantments are being requested.
     */
    @Nullable
    public Enchantment getTargetEnchant() {
        return this.targetEnchant;
    }
}
