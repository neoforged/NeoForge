/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// Fired to register [RecipePropertySet]s, for example for use in input slot validaty checks of crafting blocks.
/// Use cases which need the entire recipe on the client should use [OnDatapackSyncEvent#sendRecipes] instead.
///
/// Fires on both [physical sides][Dist] on the mod-specific event bus.
public final class RegisterRecipePropertiesEvent extends Event implements IModBusEvent {
    private final Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> propertySets;

    @ApiStatus.Internal
    public RegisterRecipePropertiesEvent(Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> propertySets) {
        this.propertySets = propertySets;
    }

    /// Register a [RecipePropertySet] with the given key and ingredient extractor.
    ///
    /// @param key       The key to register the recipe property set under
    /// @param extractor The ingredient extractor to use for collecting applicable ingredients
    /// @throws IllegalArgumentException when multiple recipe property sets are registered under the same key
    public void register(ResourceKey<RecipePropertySet> key, RecipeManager.IngredientExtractor extractor) {
        if (this.propertySets.putIfAbsent(key, extractor) != null) {
            throw new IllegalArgumentException("Duplicate property set registration for key " + key.identifier());
        }
    }
}
