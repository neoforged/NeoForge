/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around a {@link RecipeOutput} that adds conditions to all received recipes.
 * Do not use directly, obtain via {@link RecipeOutput#withConditions(ICondition...)}.
 */
@ApiStatus.Internal
public class ConditionalRecipeOutput implements RecipeOutput {
    private final RecipeOutput inner;
    private final ICondition[] conditions;

    public ConditionalRecipeOutput(RecipeOutput inner, ICondition[] conditions) {
        this.inner = inner;
        this.conditions = conditions;
    }

    @Override
    public Advancement.Builder advancement() {
        return inner.advancement();
    }

    @Override
    public ICondition[] getConditions(ICondition... other) {
        ICondition[] innerConditions;
        if (other.length == 0) {
            innerConditions = this.conditions;
        } else if (this.conditions.length == 0) {
            innerConditions = other;
        } else {
            innerConditions = ArrayUtils.addAll(this.conditions, other);
        }
        return innerConditions;
    }

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, RecipeOutput other) {
        inner.accept(id, recipe, advancement, this);
    }
}
