/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;

/**
 * Extra methods for {@link RecipeOutput}.
 */
public interface IRecipeOutputExtension {
    private RecipeOutput self() {
        return (RecipeOutput) this;
    }

    /**
     * Generates a recipe with the given conditions.
     */
    void accept(FinishedRecipe finishedRecipe, ICondition... conditions);

    /**
     * Builds a wrapper around this recipe output that adds conditions to all received recipes.
     */
    default RecipeOutput withConditions(ICondition... conditions) {
        return new ConditionalRecipeOutput(self(), conditions);
    }
}
