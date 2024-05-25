/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired whenever the enchantment level of a particular item is requested for gameplay purposes.<br>
 * It is called from {@link IItemStackExtension#getEnchantmentLevel(Enchantment)} and {@link IItemStackExtension#getAllEnchantments()}.
 * <p>
 * It is not fired for interactions with NBT, which means these changes will not reflect in the item tooltip.
 */
public class GetEnchantmentLevelEvent extends Event {
    protected final ItemStack stack;
    protected final ItemEnchantments.Mutable enchantments;
    @Nullable
    protected final Holder<Enchantment> targetEnchant;

    public GetEnchantmentLevelEvent(ItemStack stack, ItemEnchantments.Mutable enchantments, @Nullable Holder<Enchantment> targetEnchant) {
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
    public ItemEnchantments.Mutable getEnchantments() {
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
    public Holder<Enchantment> getTargetEnchant() {
        return this.targetEnchant;
    }

    /**
     * Helper method around {@link #getTargetEnchant()} that checks if the target is the specified enchantment, or if the target is null.
     * 
     * @param ench The enchantment to check.
     * @return If modifications to the passed enchantment are relevant for this event.
     * @see #getTargetEnchant() for more information about the target enchantment.
     */
    public boolean isTargetting(Holder<Enchantment> ench) {
        return this.targetEnchant == null || this.targetEnchant.is(ench);
    }

    /**
     * Helper method around {@link #getTargetEnchant()} that checks if the target is the specified enchantment, or if the target is null.
     *
     * @param ench The enchantment to check.
     * @return If modifications to the passed enchantment are relevant for this event.
     * @see #getTargetEnchant() for more information about the target enchantment.
     */
    public boolean isTargetting(ResourceKey<Enchantment> ench) {
        return this.targetEnchant == null || this.targetEnchant.is(ench);
    }
}
