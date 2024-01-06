/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import org.jetbrains.annotations.Nullable;

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
    void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, List<ICondition> conditions, List<WithConditions<Pair<Recipe<?>, Advancement>>> alternatives);

    /**
     * Builds a wrapper around this recipe output that adds conditions to all received recipes.
     */
    default ConditionalRecipeOutput withConditions(ICondition... conditions) {
        return new ConditionalRecipeOutput(self(), conditions);
    }
}
