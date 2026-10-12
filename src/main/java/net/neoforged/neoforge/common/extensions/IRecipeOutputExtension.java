/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.ConditionalRecipeOutput;
import org.jspecify.annotations.Nullable;

/**
 * Extra methods for {@link RecipeOutput}.
 */
public interface IRecipeOutputExtension extends AutoCloseable {
    private RecipeOutput self() {
        return (RecipeOutput) this;
    }

    /**
     * Generates a recipe with the given conditions.
     */
    void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions);

    /**
     * Builds a wrapper around this recipe output that adds conditions to all received recipes.
     */
    default RecipeOutput withConditions(ICondition... conditions) {
        return new ConditionalRecipeOutput(self(), conditions);
    }

    /// Sets the mod id that should be used as the namespace for generating recipes via
    /// [RecipeBuilder#save(RecipeOutput)] or [RecipeBuilder#save(RecipeOutput, String)].
    /// This is provided as a helper for existing [RecipeProvider] methods that assume that
    /// do not provide some way to set the recipe namespace.
    ///
    /// This should be scoped through a `try`-with-resources block:
    /// <!-- @formatter:off because of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5474 -->
    /// ```java
    /// // In some `RecipeProvider` subclass
    /// @Override
    /// protected void buildRecipes() {
    ///     try (var modOutput = this.output.forMod("examplemod")) {
    ///         // Sets the recipe id to "examplemod:diamond_to_apple".
    ///         this.shapeless(RecipeCategory.MISC, Items.APPLE)
    ///             .requires(Items.DIAMOND)
    ///             .unlockedBy(getHasName(Items.APPLE), this.has(Items.APPLE))
    ///             .save(this.output, "diamond_to_apple");
    ///
    ///         // ...
    ///     }
    /// }
    /// ```
    /// <!-- @formatter:on -->
    ///
    /// @param modId The mod id to use.
    /// @return This recipe output.
    default RecipeOutput forMod(String modId) {
        return self();
    }

    /// @return The mod id to use when generating a recipe through the [RecipeBuilder].
    default @Nullable String overrideModId() {
        return null;
    }

    @Override
    default void close() {}
}
