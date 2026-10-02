/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.RegisterRecipePropertiesEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class RecipePropertySetManager {
    private static final Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> RECIPE_PROPERTY_SETS = new IdentityHashMap<>();

    public static Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> captureVanillaSets(
            Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> vanillaSets) {
        RECIPE_PROPERTY_SETS.putAll(vanillaSets);
        return Collections.unmodifiableMap(RECIPE_PROPERTY_SETS);
    }

    public static void init() {
        ModLoader.postEvent(new RegisterRecipePropertiesEvent(RECIPE_PROPERTY_SETS));
    }

    private RecipePropertySetManager() {}
}
