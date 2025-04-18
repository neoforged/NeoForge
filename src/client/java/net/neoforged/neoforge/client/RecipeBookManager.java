/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterRecipeBookSearchCategoriesEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for mod-provided search {@link ExtendedRecipeBookCategory} implementations.
 */
public final class RecipeBookManager {
    private static Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> searchCategories = Map.of();

    public static Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> getSearchCategories() {
        return searchCategories;
    }

    @ApiStatus.Internal
    public static void init() {
        var searchCategories = new IdentityHashMap<ExtendedRecipeBookCategory, List<RecipeBookCategory>>();
        var event = new RegisterRecipeBookSearchCategoriesEvent(searchCategories);
        ModLoader.postEvent(event);
        RecipeBookManager.searchCategories = Collections.unmodifiableMap(searchCategories);
    }
}
