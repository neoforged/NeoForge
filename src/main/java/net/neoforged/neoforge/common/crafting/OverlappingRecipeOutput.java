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
import org.jetbrains.annotations.Nullable;

public class OverlappingRecipeOutput implements RecipeOutput {
    private final RecipeOutput inner;
    @Nullable
    private final ResourceLocation override;

    public OverlappingRecipeOutput(RecipeOutput inner, ResourceLocation override) {
        this.inner = inner;
        this.override = override;
    }

    @Override
    public Advancement.Builder advancement() {
        return inner.advancement();
    }

    @Nullable
    public ResourceLocation getOutputOverride() {
        return override;
    }

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, RecipeOutput other) {
        inner.accept(id, recipe, advancement, this);
    }
}
