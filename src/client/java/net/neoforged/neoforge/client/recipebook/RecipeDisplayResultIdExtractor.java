/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.recipebook;

import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;

/// Represents logic for extracting all relevant ids from a recipe display's result slot display.
@FunctionalInterface
public interface RecipeDisplayResultIdExtractor {
    /// {@return all relevant ids from a recipe display's result slot display}
    ///
    /// @param result         The result slot display from the recipe display being checked.
    /// @param registryAccess Current registry access.
    /// @param recipeContext  Context data for recipes.
    ///
    /// @see #forRegisteredType Simple implementation for registry based types
    Stream<Identifier> extract(SlotDisplay result, HolderLookup.Provider registryAccess, ContextMap recipeContext);

    /// Helper to create an id extractor for registered elements.
    ///
    /// @param contentsFactory Factory to resolve the typed instances from the result display.
    static <TYPE> RecipeDisplayResultIdExtractor forRegisteredType(DisplayContentsFactory<? extends TypedInstance<TYPE>> contentsFactory) {
        return (result, _, recipeContext) -> {
            return result.resolve(recipeContext, contentsFactory).
                    flatMap(stack -> stack.typeHolder().unwrapKey().stream())
                    .map(ResourceKey::identifier);
        };
    }
}
