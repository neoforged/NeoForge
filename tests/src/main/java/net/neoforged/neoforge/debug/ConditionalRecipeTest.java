/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "conditional_recipes")
public interface ConditionalRecipeTest {
    @TestHolder(description = "Validates that recipes support conditionals by generating a new recipe disabled by the FALSE condition", enabledByDefault = true)
    static void testConditionalRecipe(DynamicTest test, RegistrationHelper reg) {
        // name pointing to recipe which should never be enabled
        var recipeName = ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "always_disabled_recipe"));

        reg.addClientProvider(event -> new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new RecipeProvider(registries, output) {
                    @Override
                    protected void buildRecipes() {
                        // generic stone -> bedrock recipe
                        shapeless(RecipeCategory.MISC, Items.BEDROCK)
                                .requires(Items.STONE)
                                .unlockedBy("has_stone", has(Items.STONE))
                                // false condition to have this recipe always disabled
                                .save(output.withConditions(FalseCondition.INSTANCE), recipeName);
                    }
                };
            }

            @Override
            public String getName() {
                return "always_disabled_recipe_provider";
            }
        });

        test.eventListeners().forge().addListener((ServerStartedEvent event) -> {
            var recipe = event.getServer().getRecipeManager().recipeMap().byKey(recipeName);

            if (recipe == null)
                test.pass();
            else
                test.fail("Found recipe: '" + recipeName.location() + "', This should always be disabled due to 'FALSE' condition!");
        });
    }
}
