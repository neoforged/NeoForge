/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

public interface IExtensionSlot {
    // Context
    IExtensionSlotSource getContainer();

    ResourceLocation getType();

    // Access
    ItemStack getContents();

    void setContents(ItemStack stack);

    void onContentsChanged();

    /**
     * @return A tag key for the items allowed in this slot. `null` allows all items.
     */
    @Nullable
    TagKey<Item> getEquipTag();

    private boolean checkTag(ItemStack stack) {
        var tag = getEquipTag();
        return tag == null || stack.is(tag);
    }

    // Permissions

    /**
     * Queries wether or not the stack can be placed in this slot.
     *
     * @param stack The ItemStack in the slot.
     */
    default boolean canEquip(ItemStack stack) {
        var cap = stack.getCapability(ExtensionSlotItemCapability.INSTANCE);
        return (cap == null || cap.canEquip(stack, this)) && checkTag(stack);
    }

    /**
     * Queries wether or not the stack can be removed from this slot.
     *
     * @param stack The ItemStack in the slot.
     */
    default boolean canUnequip(ItemStack stack) {
        var cap = stack.getCapability(ExtensionSlotItemCapability.INSTANCE);
        return cap != null
                ? cap.canUnequip(stack, this)
                : !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }
}
