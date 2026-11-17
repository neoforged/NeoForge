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
import net.neoforged.neoforge.client.RecipeBookManager.RecipeDisplayResultInfo;
import net.neoforged.neoforge.client.recipebook.RecipeDisplayResultIdExtractor;
import net.neoforged.neoforge.client.recipebook.RecipeDisplayResultTooltipExtractor;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event to register {@link ExtendedRecipeBookCategory} instances for search.
 * Modded equivalent of vanilla's {@link SearchRecipeBookCategory}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class RegisterRecipeBookSearchCategoriesEvent extends Event implements IModBusEvent {//TODO - 26.4: Rename to RegisterRecipeBookSearchDataEvent
    private final Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> categories;
    private final List<RecipeDisplayResultInfo> recipeDisplaySearchInfo;

    @ApiStatus.Internal
    public RegisterRecipeBookSearchCategoriesEvent(Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> categories,
            List<RecipeDisplayResultInfo> recipeDisplaySearchInfo) {
        this.categories = categories;
        this.recipeDisplaySearchInfo = recipeDisplaySearchInfo;
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

    /// Registers a recipe result indexer to allow for having search index things other than just item results in recipes.
    /// 
    /// @param tooltipExtractor Logic to extract the tooltip from the result display.
    /// @param idExtractor      Logic to extract the element's id from the result display.
    /// @apiNote NeoForge registers indexers for any recipes that may expose fluids in their result.
    /// @see RecipeDisplayResultTooltipExtractor#forStack
    /// @see RecipeDisplayResultIdExtractor#forRegisteredType
    public void registerResultIndexer(RecipeDisplayResultTooltipExtractor tooltipExtractor, RecipeDisplayResultIdExtractor idExtractor) {
        recipeDisplaySearchInfo.add(new RecipeDisplayResultInfo(tooltipExtractor, idExtractor));
    }
}
