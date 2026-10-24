/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackContentsFactory;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterRecipeBookSearchCategoriesEvent;
import net.neoforged.neoforge.client.recipebook.RecipeDisplayResultIdExtractor;
import net.neoforged.neoforge.client.recipebook.RecipeDisplayResultTooltipExtractor;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for mod-provided search {@link ExtendedRecipeBookCategory} implementations.
 */
public final class RecipeBookManager {//TODO - 26.4: Move this to the recipebook subpackage
    private static Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> searchCategories = Map.of();
    private static List<RecipeDisplayResultInfo> recipeDisplaySearchInfo = Collections.emptyList();

    public static Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> getSearchCategories() {
        return searchCategories;
    }

    @ApiStatus.Internal
    public static void init() {
        var searchCategories = new IdentityHashMap<ExtendedRecipeBookCategory, List<RecipeBookCategory>>();
        List<RecipeDisplayResultInfo> recipeDisplaySearchInfo = new ArrayList<>();
        var event = new RegisterRecipeBookSearchCategoriesEvent(searchCategories, recipeDisplaySearchInfo);
        //Add vanilla's default tooltip handling for items, optimized to only have to look up the item registry once
        event.registerResultIndexer(RecipeDisplayResultTooltipExtractor.forStack(ItemStackContentsFactory.INSTANCE, ItemStack::getTooltipLines),
                (result, _, registry, recipeContext) -> result.resolve(recipeContext, ItemStackContentsFactory.INSTANCE)
                        .<Identifier>map(stack -> registry.getKey(stack.typeHolder().value())));
        ModLoader.postEvent(event);
        RecipeBookManager.searchCategories = Collections.unmodifiableMap(searchCategories);
        RecipeBookManager.recipeDisplaySearchInfo = Collections.unmodifiableList(recipeDisplaySearchInfo);
    }

    @ApiStatus.Internal
    public static Stream<String> getTooltipLines(RecipeCollection collection, ContextMap recipeContext, Item.TooltipContext context, TooltipFlag tooltipFlag) {
        return collection.getRecipes().stream()
                .flatMap(e -> {
                    SlotDisplay result = e.display().result();
                    return recipeDisplaySearchInfo.stream().flatMap(info -> info.tooltipExtractor.extract(result, recipeContext, context, tooltipFlag));
                })
                .map(l -> ChatFormatting.stripFormatting(l.getString()).trim())
                .filter(s -> !s.isEmpty());
    }

    @ApiStatus.Internal
    public static Stream<Identifier> getResultIds(RecipeDisplayEntry entry, RegistryAccess registryAccess, Registry<Item> itemRegistries, ContextMap recipeContext) {
        SlotDisplay result = entry.display().result();
        return recipeDisplaySearchInfo.stream().flatMap(info -> info.idExtractor.extract(result, registryAccess, itemRegistries, recipeContext));
    }

    @ApiStatus.Internal
    public record RecipeDisplayResultInfo(RecipeDisplayResultTooltipExtractor tooltipExtractor, RecipeDisplayResultIdExtractor idExtractor) {}
}
