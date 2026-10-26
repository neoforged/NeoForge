/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.recipebook;

import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// Represents logic for extracting all relevant ids from a recipe display's result slot display.
@FunctionalInterface
public interface RecipeDisplayResultIdExtractor {
    /// {@return all relevant ids from a recipe display's result slot display}
    ///
    /// @param result         The result slot display from the recipe display being checked.
    /// @param registryAccess Current registry access.
    /// @param itemRegistries Cached lookup of the item registries.
    /// @param recipeContext  Context data for recipes.
    ///
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
