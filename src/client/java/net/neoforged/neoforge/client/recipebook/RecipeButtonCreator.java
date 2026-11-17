/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.recipebook;

import java.util.function.Predicate;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;

@FunctionalInterface
public interface RecipeButtonCreator {
    /// Creates a new recipe button.
    RecipeButton create(SlotSelectTime slotSelectTime, RecipeBookComponent.RecipePlacer recipePlacer, Predicate<RecipeButton> tryOpenOverlay);
}
