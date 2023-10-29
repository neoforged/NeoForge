/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom categories for the vanilla recipe book, making it usable in modded GUIs.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterRecipeBookCategoriesEvent extends Event implements IModBusEvent {
    private final Map<RecipeBookCategories, ImmutableList<RecipeBookCategories>> aggregateCategories;
    private final Map<RecipeBookType, ImmutableList<RecipeBookCategories>> typeCategories;
    private final Map<RecipeType<?>, Function<RecipeHolder<?>, RecipeBookCategories>> recipeCategoryLookups;

    @ApiStatus.Internal
    public RegisterRecipeBookCategoriesEvent(
            Map<RecipeBookCategories, ImmutableList<RecipeBookCategories>> aggregateCategories,
            Map<RecipeBookType, ImmutableList<RecipeBookCategories>> typeCategories,
            Map<RecipeType<?>, Function<RecipeHolder<?>, RecipeBookCategories>> recipeCategoryLookups) {
        this.aggregateCategories = aggregateCategories;
        this.typeCategories = typeCategories;
        this.recipeCategoryLookups = recipeCategoryLookups;
    }

    /**
     * Registers the list of categories that compose an aggregate category.
     */
    public void registerAggregateCategory(RecipeBookCategories category, List<RecipeBookCategories> others) {
        aggregateCategories.put(category, ImmutableList.copyOf(others));
    }

    /**
     * Registers the list of categories that compose a recipe book.
     */
    public void registerBookCategories(RecipeBookType type, List<RecipeBookCategories> categories) {
        typeCategories.put(type, ImmutableList.copyOf(categories));
    }

    /**
     * Registers a category lookup for a certain recipe type.
     */
    public void registerRecipeCategoryFinder(RecipeType<?> type, Function<RecipeHolder<?>, RecipeBookCategories> lookup) {
        recipeCategoryLookups.put(type, lookup);
    }
}
