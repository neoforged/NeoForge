/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Starting from 1.20.5 this is used to hold {@link IBrewingRecipe}s inside of {@link PotionBrewing}.
 * For queries, use the vanilla {@link PotionBrewing}.
 * For registration, use {@link RegisterBrewingRecipesEvent}.
 */
@ApiStatus.Internal
public record BrewingRecipeRegistry(List<IBrewingRecipe> recipes) {
    /**
     * Returns the output ItemStack obtained by brewing the passed input and
     * ingredient.
     */
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (input.isEmpty() || input.getCount() != 1) return ItemStack.EMPTY;
        if (ingredient.isEmpty()) return ItemStack.EMPTY;

        for (IBrewingRecipe recipe : recipes) {
            ItemStack output = recipe.getOutput(input, ingredient);
            if (!output.isEmpty()) {
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns true if the passed input and ingredient have an output
     */
    public boolean hasOutput(ItemStack input, ItemStack ingredient) {
        return !getOutput(input, ingredient).isEmpty();
    }

    /**
     * Returns true if the passed ItemStack is a valid ingredient for any of the
     * recipes in the registry.
     */
    public boolean isValidIngredient(ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (IBrewingRecipe recipe : recipes) {
            if (recipe.isIngredient(stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the passed ItemStack is a valid input for any of the
     * recipes in the registry.
     */
    public boolean isValidInput(ItemStack stack) {
        for (IBrewingRecipe recipe : recipes) {
            if (recipe.isInput(stack)) {
                return true;
            }
        }
        return false;
    }
}
