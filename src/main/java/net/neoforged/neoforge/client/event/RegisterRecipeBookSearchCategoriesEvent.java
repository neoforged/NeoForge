/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event to register {@link ExtendedRecipeBookCategory} instances for search.
 * Modded equivalent of vanilla's {@link SearchRecipeBookCategory}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class RegisterRecipeBookSearchCategoriesEvent extends Event implements IModBusEvent {
    private final Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> categories;

    @ApiStatus.Internal
    public RegisterRecipeBookSearchCategoriesEvent(Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> categories) {
        this.categories = categories;
    }

    public void register(ExtendedRecipeBookCategory searchCategory, RecipeBookCategory... includedCategories) {
        if (includedCategories.length == 0) {
            throw new IllegalArgumentException("Forgot to register included categories.");
        }
        if (categories.containsKey(searchCategory)) {
            throw new IllegalArgumentException("Duplicate registration of search category " + searchCategory);
        }
        categories.put(searchCategory, List.of(includedCategories));
    }
}
