/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.datafixers.util.Function4;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent.OverlayRecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay.ItemStackContentsFactory;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterRecipeBookSearchCategoriesEvent;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

/**
 * Manager for mod-provided search {@link ExtendedRecipeBookCategory} implementations.
 */
public final class RecipeBookManager {
    private static Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> searchCategories = Map.of();
    private static List<RecipeDisplayResultInfo> recipeDisplaySearchInfo = Collections.emptyList();

    public static Map<ExtendedRecipeBookCategory, List<RecipeBookCategory>> getSearchCategories() {
        return searchCategories;
    }

    @Internal
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

    @Internal
    public static Stream<String> getTooltipLines(RecipeCollection collection, ContextMap recipeContext, Item.TooltipContext context, TooltipFlag tooltipFlag) {
        return collection.getRecipes().stream()
                .flatMap(e -> {
                    SlotDisplay result = e.display().result();
                    return recipeDisplaySearchInfo.stream().flatMap(info -> info.tooltipExtractor.extract(result, recipeContext, context, tooltipFlag));
                })
                .map(l -> ChatFormatting.stripFormatting(l.getString()).trim())
                .filter(s -> !s.isEmpty());
    }

    @Internal
    public static Stream<Identifier> getResultIds(RecipeDisplayEntry entry, RegistryAccess registryAccess, Registry<Item> itemRegistries, ContextMap recipeContext) {
        SlotDisplay result = entry.display().result();
        return recipeDisplaySearchInfo.stream().flatMap(info -> info.idExtractor.extract(result, registryAccess, itemRegistries, recipeContext));
    }

    @Internal
    public record RecipeDisplayResultInfo(RecipeDisplayResultTooltipExtractor tooltipExtractor, RecipeDisplayResultIdExtractor idExtractor) {}

    /// Represents logic for extracting all relevant tooltips for a recipe display's result slot display.
    @FunctionalInterface
    public interface RecipeDisplayResultTooltipExtractor {
        /// {@return all relevant tooltips for a recipe display's result slot display}
        /// 
        /// @param result        The result slot display from the recipe display being checked.
        /// @param recipeContext Context data for recipes.
        /// @param context       Tooltip context to use for retrieving tooltips.
        /// @param tooltipFlag   Tooltip flag to use for retrieving tooltips.
        /// @see #forStack Simple implementation for types that mirror ItemStack's tooltip getter
        Stream<Component> extract(SlotDisplay result, ContextMap recipeContext, Item.TooltipContext context, TooltipFlag tooltipFlag);

        /// Helper to create a tooltip extractor capable of querying tooltips with the same method signature as items.
        ///
        /// @param contentsFactory Factory to resolve "stacks" from the result display.
        /// @param tooltipGetter   Method reference to get tooltips from the "stack".
        static <STACK> RecipeDisplayResultTooltipExtractor forStack(DisplayContentsFactory<STACK> contentsFactory,
                Function4<STACK, Item.TooltipContext, @Nullable Player, TooltipFlag, List<Component>> tooltipGetter) {
            return (result, recipeContext, context, tooltipFlag) -> result.resolve(recipeContext, contentsFactory)
                    .flatMap(stack -> tooltipGetter.apply(stack, context, context.player(), tooltipFlag).stream());
        }
    }

    /// Represents logic for extracting all relevant ids from a recipe display's result slot display.
    @FunctionalInterface
    public interface RecipeDisplayResultIdExtractor {
        /// {@return all relevant ids from a recipe display's result slot display}
        /// 
        /// @param result         The result slot display from the recipe display being checked.
        /// @param registryAccess Current registry access.
        /// @param itemRegistries Cached lookup of the item registries.
        /// @param recipeContext  Context data for recipes.
        /// @see #forRegisteredType Simple implementation for registry based types
        Stream<Identifier> extract(SlotDisplay result, RegistryAccess registryAccess, Registry<Item> itemRegistries, ContextMap recipeContext);

        /// Helper to create an id extractor for registered elements.
        ///
        /// @param contentsFactory Factory to resolve the typed instances from the result display.
        /// @param registryName    Name of the registry for objects of the provided type.
        static <TYPE> RecipeDisplayResultIdExtractor forRegisteredType(DisplayContentsFactory<? extends TypedInstance<TYPE>> contentsFactory,
                ResourceKey<? extends Registry<TYPE>> registryName) {
            return (result, registryAccess, _, recipeContext) -> {
                Registry<TYPE> registry = registryAccess.lookupOrThrow(registryName);
                return result.resolve(recipeContext, contentsFactory).<Identifier>map(stack -> registry.getKey(stack.typeHolder().value()));
            };
        }
    }

    @FunctionalInterface
    public interface RecipeOverlayButtonCreator {
        /// Creates a new overlay recipe button.
        ///
        /// @param parent      Overlay recipe component to treat as the captured `this` instance.
        /// @param x           X position of the button.
        /// @param y           Y position of the button.
        /// @param id          Id representation of the recipe the button is being created for.
        /// @param recipe      Display of the recipe the button is being created for.
        /// @param context     Context data for recipes.
        /// @param isCraftable `true` if the recipe is craftable from the contents in the player's inventory.
        OverlayRecipeButton create(OverlayRecipeComponent parent, int x, int y, RecipeDisplayId id, RecipeDisplay recipe, ContextMap context, boolean isCraftable);
    }
}
