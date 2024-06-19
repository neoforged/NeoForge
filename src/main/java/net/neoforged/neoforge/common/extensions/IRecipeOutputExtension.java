/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import net.neoforged.neoforge.common.crafting.OverlappingRecipeOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Extra methods for {@link RecipeOutput}.
 */
public interface IRecipeOutputExtension {
    private RecipeOutput self() {
        return (RecipeOutput) this;
    }

    /**
     * Generates a recipe with the given other recipe output.
     */
    void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, RecipeOutput other);

    /**
     * Builds a wrapper around this recipe output that adds conditions to all received recipes.
     */
    default RecipeOutput withConditions(ICondition... conditions) {
        return new ConditionalRecipeOutput(self(), conditions);
    }

    default ICondition[] getConditions(ICondition... other) {
        return new ICondition[0];
    }

    /**
     * Builds a wrapper around this recipe output that adds an output override to all received recipes.
     */
    default RecipeOutput withOverrides(ResourceLocation outputOverride) {
        return new OverlappingRecipeOutput(self(), outputOverride);
    }

    @Nullable
    default ResourceLocation getOutputOverride() {
        return null;
    }
}
