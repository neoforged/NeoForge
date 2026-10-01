/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.debug.block.BlockTests;
import net.neoforged.neoforge.event.ModifyRecipeJsonsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.impl.test.MethodBasedEventTest;
import org.jspecify.annotations.Nullable;

@ForEachTest(groups = { BlockTests.GROUP + ".event", "event" })
public class ModifyRecipeJsonEventTest {
    /// The recipe that [#exposesMutableRecipeJsons] adds through the event, and that [#addedRecipeIsLoaded] then looks for.
    private static final ResourceKey<Recipe<?>> ADDED_RECIPE = ResourceKey.create(
        Registries.RECIPE, Identifier.fromNamespaceAndPath("neoforge_testframework", "added_by_modify_recipe_jsons_event")
    );

    @TestHolder(
        description = "Tests that ModifyRecipeJsonsEvent exposes the raw recipe JSONs, a mutable map, mutable values and a registry lookup.",
        enabledByDefault = true
    )
    static void exposesMutableRecipeJsons(final ModifyRecipeJsonsEvent event, final MethodBasedEventTest test) {
        // Grab the map of recipe JSONs from the event.
        Map<Identifier, JsonElement> recipeJsons = event.getRecipeJsons();
        if (recipeJsons.isEmpty()) {
            test.fail("ModifyRecipeJsonsEvent did not expose any recipe JSONs");
            return;
        }

        JsonObject added = copyOfAnyRecipe(recipeJsons);
        if (added == null) {
            test.fail("ModifyRecipeJsonsEvent did not expose any usable recipe JSON");
            return;
        }

        // Ensure the map itself is mutable by registering the copy under a new id.
        recipeJsons.put(ADDED_RECIPE.identifier(), added);

        // Ensure the values of the map are mutable as well. This is undone right away, the recipe we add has to stay loadable.
        added.addProperty("neoforge_testframework", "test");
        if (!added.has("neoforge_testframework")) {
            test.fail("Recipe JSONs of the map are not mutable");
            return;
        }
        added.remove("neoforge_testframework");

        // Ensure the event exposes registry access. Both of these throw rather than returning an empty result.
        event.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);

        test.pass();
    }

    @TestHolder(
        description = "Tests that a recipe added through ModifyRecipeJsonsEvent is deserialized and ends up in the recipe manager.",
        enabledByDefault = true
    )
    static void addedRecipeIsLoaded(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ServerStartedEvent event) -> {
            if (event.getServer().getRecipeManager().recipeMap().byKey(ADDED_RECIPE) == null) {
                test.fail("Recipe " + ADDED_RECIPE.identifier() + " added through the event was not loaded");
                return;
            }
            test.pass();
        });
    }

    /// Copies any recipe that produces a result, so that what we add is a recipe that deserializes on its own. Conditions are
    /// dropped, since they could disable the copy again while the test expects to find it in the recipe manager.
    private static @Nullable JsonObject copyOfAnyRecipe(Map<Identifier, JsonElement> recipeJsons) {
        for (JsonElement json : recipeJsons.values()) {
            if (json instanceof JsonObject object && object.has("result")) {
                JsonObject copy = object.deepCopy();
                copy.remove(ConditionalOps.DEFAULT_CONDITIONS_KEY);
                return copy;
            }
        }
        return null;
    }
}