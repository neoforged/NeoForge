/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.FuelValues;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public interface ItemInstanceExtension {
    /**
     * ItemStack sensitive version of {@link Item#getCraftingRemainder()}.
     * Returns a full ItemStack instance of the result.
     *
     * @return The resulting ItemStack
     */
    default @Nullable ItemStackTemplate getCraftingRemainder() {
        return self().typeHolder().value().getCraftingRemainder(self());
    }

    /**
     * Returns the fuel burn time for this item stack. If it is zero, this item is not a fuel.
     * <p>
     * Will never return a negative value.
     *
     * @return the fuel burn time for this item stack in a furnace.
     * @apiNote This method by default returns the {@code burn_time} specified in
     *          the {@code furnace_fuels.json} file.
     */
    default int getBurnTime(@Nullable RecipeType<?> recipeType, FuelValues fuelValues) {
        int burnTime = self().typeHolder().value().getBurnTime(self(), recipeType, fuelValues);
        if (burnTime < 0) {
            throw new IllegalStateException("Stack of item " + self().typeHolder().getRegisteredName() + " has a negative burn time");
        }
        return EventHooks.getItemBurnTime(self(), burnTime, recipeType, fuelValues);
    }

    /**
     * Override this to set a non-default armor slot for an ItemStack, but <em>do
     * not use this to get the armor slot of said stack; for that, use
     * {@link LivingEntity#getEquipmentSlotForItem(ItemStack)}.</em>
     *
     * @return the armor slot of the ItemStack, or {@code null} to let the default
     *         vanilla logic as per {@code LivingEntity.getSlotForItemStack(stack)}
     *         decide
     */
    @Nullable
    default EquipmentSlot getEquipmentSlot() {
        return self().typeHolder().value().getEquipmentSlot(self());
    }

    /**
     * Queries if an item can perform the given action.
     * See {@link ItemAbilities} for a description of each stock action
     *
     * @param itemAbility The action being queried
     * @return True if the stack can perform the action
     */
    default boolean canPerformAction(ItemAbility itemAbility) {
        return self().typeHolder().value().canPerformAction(self(), itemAbility);
    }

    /**
     * Gets all enchantments from NBT. Use {@link ItemStack#getAllEnchantments} for gameplay logic.
     */
    default ItemEnchantments getTagEnchantments() {
        return self().getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    /**
     * Gets the gameplay level of the target enchantment on this stack.
     * <p>
     * Use in place of {@link EnchantmentHelper#getTagEnchantmentLevel} for gameplay logic.
     * <p>
     * Use {@link EnchantmentHelper#getEnchantmentsForCrafting} and {@link EnchantmentHelper#setEnchantments} when modifying the item's enchantments.
     *
     * @param enchantment The enchantment being checked for.
     * @return The level of the enchantment, or 0 if not present.
     * @see {@link IItemStackExtension#getAllEnchantments} to get all gameplay enchantments
     */
    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        int level = self().typeHolder().value().getEnchantmentLevel(self(), enchantment);
        return EventHooks.getEnchantmentLevelSpecific(level, self(), enchantment);
    }

    private ItemInstance self() {
        return (ItemInstance) this;
    }
}
