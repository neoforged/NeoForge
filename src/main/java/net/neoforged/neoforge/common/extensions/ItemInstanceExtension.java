/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
     * Retrieves the normal 'lifespan' of this item when it is dropped on the ground
     * as a EntityItem. This is in ticks, standard result is 6000, or 5 mins.
     *
     * @param level The level the entity is in
     * @return The normal lifespan in ticks.
     */
    default int getEntityLifespan(Level level) {
        return self().typeHolder().value().getEntityLifespan(self(), level);
    }

    /**
     * Determines the amount of durability the mending enchantment
     * will repair, on average, per point of experience.
     */
    default float getXpRepairRatio() {
        return self().typeHolder().value().getXpRepairRatio(self());
    }

    /**
     * Determines if the specific ItemStack can be placed in the specified armor
     * slot, for the entity.
     *
     * @param armorType Armor slot to be verified.
     * @param entity    The entity trying to equip the armor
     * @return True if the given ItemStack can be inserted in the slot
     */
    default boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        return self().typeHolder().value().canEquip(self(), armorType, entity);
    }

    /**
     *
     * Should this item, when held, allow sneak-clicks to pass through to the underlying block?
     *
     * @param level  The level
     * @param pos    Block position in level
     * @param player The Player that is wielding the item
     */
    default boolean doesSneakBypassUse(LevelReader level, BlockPos pos, Player player) {
        return self().typeHolder().value().doesSneakBypassUse(self(), level, pos, player);
    }

    /**
     * Called by Piglins when checking to see if they will give an item or something in exchange for this item.
     *
     * @return True if this item can be used as "currency" by piglins
     */
    default boolean isPiglinCurrency() {
        return self().typeHolder().value().isPiglinCurrency(self());
    }

    /**
     * Called by Piglins to check if a given item prevents hostility on sight. If this is true the Piglins will be neutral to the entity wearing this item, and will not
     * attack on sight. Note: This does not prevent Piglins from becoming hostile due to other actions, nor does it make Piglins that are already hostile stop being so.
     *
     * @param wearer The entity wearing this ItemStack
     *
     * @return True if piglins are neutral to players wearing this item in an armor slot
     */
    default boolean makesPiglinsNeutral(LivingEntity wearer) {
        return self().typeHolder().value().makesPiglinsNeutral(self(), wearer);
    }

    /**
     * Whether this {@link Item} can be used to hide player's gaze from Endermen and Creakings.
     *
     * @param player The player watching the entity
     * @param entity The entity the player is looking at, may be null
     * @return true if this {@link Item} hides the player's gaze from the given entity
     */
    default boolean isGazeDisguise(Player player, @Nullable LivingEntity entity) {
        return self().typeHolder().value().isGazeDisguise(self(), player, entity);
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
