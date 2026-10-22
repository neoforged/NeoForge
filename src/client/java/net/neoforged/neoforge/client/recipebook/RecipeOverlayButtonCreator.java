/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.recipebook;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

@FunctionalInterface
public interface RecipeOverlayButtonCreator {
    /// Creates a new overlay recipe button.
    ///
    /// @param parent      Overlay recipe component to treat as the captured `this` instance.
    /// @param x           X position of the button.
    /// @param y           Y position of the button.
    /// @param id          Id representation of the recipe the button is being created for.
    /// @param recipe      Display of the recipe the button is being created for.
    /// @param context     Context data for recipes.
    /// @param isCraftable `true` if the recipe is craftable from the contents in the player's inventory.
    OverlayRecipeButton create(OverlayRecipeComponent parent, int x, int y, RecipeDisplayId id, RecipeDisplay recipe, ContextMap context, boolean isCraftable);
}
