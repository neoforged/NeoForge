/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.UnaryOperator;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.data.RecipeContextBuilder;
import net.neoforged.neoforge.common.data.RecipeOutputWrapper;
import net.neoforged.neoforge.common.data.ScopedRecipeOutput;
import org.jspecify.annotations.Nullable;

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
    void accept(ResourceKey<Recipe<?>> key, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions);

    /// Builds a wrapper around this recipe output that adds conditions to all received recipes.
    ///
    /// @param conditions The conditions to apply to the recipe output.
    /// @return The wrapped recipe output.
    default RecipeOutput withConditions(ICondition... conditions) {
        return RecipeOutputWrapper.singleUse(self(), builder -> builder.withConditions(conditions));
    }

    /// Scopes the [RecipeOutput] to use a specific context.
    ///
    /// This should be used via a `try`-with-resources block:
    /// <!-- @formatter:off because of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5474 -->
    /// ```java
    /// // In some `RecipeProvider` subclass
    /// @Override
    /// protected void buildRecipes() {
    ///     try (var _ = this.output.withContext(
    ///             builder -> builder.forMod("examplemod")
    ///                 .withConditions(
    ///                     NeoForgeConditions.modLoaded("examplemod")
    ///                 )
    ///     )) {
    ///         // Sets the recipe id to "examplemod:diamond_to_apple".
    ///         // Will only load if "examplemod" is present.
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
    /// @param builder A unary operator used to construct the context.
    /// @return A scoped recipe output.
    default ScopedRecipeOutput withContext(UnaryOperator<RecipeContextBuilder> builder) {
        return RecipeOutputWrapper.withContext(self(), builder);
    }
}
