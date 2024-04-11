/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import static net.minecraft.world.item.alchemy.PotionBrewing.Builder;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

/**
 * Interface for more flexible brewing recipes.
 *
 * <p>Register using {@link RegisterBrewingRecipesEvent} and {@link Builder#addRecipe(IBrewingRecipe)}.
 */
public interface IBrewingRecipe {
    /**
     * Returns true is the passed ItemStack is an input for this recipe. "Input"
     * being the item that goes in one of the three bottom slots of the brewing
     * stand (e.g: water bottle)
     */
    boolean isInput(ItemStack input);

    /**
     * Returns true if the passed ItemStack is an ingredient for this recipe.
     * "Ingredient" being the item that goes in the top slot of the brewing
     * stand (e.g: nether wart)
     */
    boolean isIngredient(ItemStack ingredient);

    /**
     * Returns the output when the passed input is brewed with the passed
     * ingredient. Empty if invalid input or ingredient.
     */
    ItemStack getOutput(ItemStack input, ItemStack ingredient);
}
