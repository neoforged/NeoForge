/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Used in BrewingRecipeRegistry to maintain the vanilla behaviour.
 * <p>
 * Most of the code was simply adapted from net.minecraft.tileentity.TileEntityBrewingStand
 */
public final class VanillaBrewingRecipe implements IBrewingRecipe {
    public static final RecipeHolder<IBrewingRecipe> INSTANCE = new RecipeHolder<>(new ResourceLocation("vanilla"), new VanillaBrewingRecipe());

    private VanillaBrewingRecipe() {}

    public static Optional<? extends RecipeHolder<IBrewingRecipe>> getFallback(Level level, IBrewingContainer brewingContainer) {
        if (INSTANCE.value().matches(brewingContainer, level)) {
            return Optional.of(INSTANCE);
        }
        return Optional.empty();
    }

    @Override
    public RecipeSerializer<VanillaBrewingRecipe> getSerializer() {
        return null;
    }

    /**
     * Code adapted from TileEntityBrewingStand.isItemValidForSlot(int index, ItemStack stack)
     */
    @Override
    public boolean isInput(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION || item == Items.GLASS_BOTTLE;
    }

    /**
     * Code adapted from TileEntityBrewingStand.isItemValidForSlot(int index, ItemStack stack)
     */
    @Override
    public boolean isCatalyst(ItemStack stack) {
        return PotionBrewing.isIngredient(stack);
    }

    /**
     * Code copied from TileEntityBrewingStand.brewPotions()
     * It brews the potion by doing the bit-shifting magic and then checking if the new PotionEffect list is different to the old one,
     * or if the new potion is a splash potion when the old one wasn't.
     */
    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!input.isEmpty() && !ingredient.isEmpty() && isCatalyst(ingredient)) {
            ItemStack result = PotionBrewing.mix(ingredient, input);
            if (result != input) {
                return result;
            }
            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }
}
