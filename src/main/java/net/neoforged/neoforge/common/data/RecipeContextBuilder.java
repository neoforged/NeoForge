/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

/// A builder for constructing the new context the recipe output should use.
public final class RecipeContextBuilder {
    private @Nullable String modId;
    private ICondition[] conditions = RecipeOutputWrapper.EMPTY;

    RecipeContextBuilder(@Nullable String parentModId) {
        this.modId = parentModId;
    }

    /// Sets the mod id that should be used as the namespace for generating recipes via
    /// [RecipeBuilder#save(RecipeOutput)] or [RecipeBuilder#save(RecipeOutput, String)].
    /// This is provided as a helper for existing [RecipeProvider] methods that do not
    /// provide some way to set the recipe namespace.
    ///
    /// @param modId The mod id to use.
    /// @return This builder.
    public RecipeContextBuilder forMod(@Nullable String modId) {
        this.modId = modId;
        return this;
    }

    /// Sets the conditions to apply to all received recipes.
    ///
    /// @param conditions The conditions to apply to the recipe output.
    /// @return This builder.
    public RecipeContextBuilder withConditions(ICondition... conditions) {
        this.conditions = conditions;
        return this;
    }

    RecipeOutputWrapper.Context build(ICondition[] parentConditions) {
        return new RecipeOutputWrapper.Context(this.modId, RecipeOutputWrapper.mergeConditions(parentConditions, this.conditions));
    }
}
