/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.data.RecipePrioritiesProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "crafting.recipe_priorities")
public class RecipePrioritiesTest {
    @GameTest
    @EmptyTemplate()
    @TestHolder(description = "Tests creating a recipe with a higher priority than vanilla recipes")
    static void testOverridingRecipe(final DynamicTest test, final RegistrationHelper reg) {
        reg.addClientProvider(event -> new RecipePrioritiesProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), "neotests_recipe_priorities") {
            @Override
            protected void start() {
                add("higher_priority_test", 1);
            }
        });
        reg.addClientProvider(event -> new Recipes.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()));
        test.onGameTest(helper -> helper
                .startSequence()
                .thenExecute(() -> helper.setBlock(1, 1, 1, Blocks.CRAFTER.defaultBlockState().setValue(BlockStateProperties.ORIENTATION, FrontAndTop.UP_NORTH).setValue(CrafterBlock.CRAFTING, true)))
                .thenExecute(() -> helper.setBlock(1, 2, 1, Blocks.CHEST))

                // Try to craft default bed recipe
                .thenMap(() -> helper.getBlockEntity(1, 1, 1, CrafterBlockEntity.class))
                .thenExecute(crafter -> crafter.setItem(3, Items.YELLOW_WOOL.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(4, Items.YELLOW_WOOL.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(5, Items.YELLOW_WOOL.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(6, Items.OAK_PLANKS.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(7, Items.OAK_PLANKS.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(8, Items.OAK_PLANKS.getDefaultInstance()))
                .thenIdle(3)
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.YELLOW_BED)) // Should craft yellow bed from recipe of yellow wool and oak planks (part of the #planks tag)

                .thenIdle(5) // Crafter cooldown

                // Try to craft recipe that overrides the default bed recipe
                .thenExecute(crafter -> crafter.setItem(3, Items.YELLOW_WOOL.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(4, Items.YELLOW_WOOL.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(5, Items.YELLOW_WOOL.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(6, Items.CHERRY_PLANKS.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(7, Items.CHERRY_PLANKS.getDefaultInstance()))
                .thenExecute(crafter -> crafter.setItem(8, Items.CHERRY_PLANKS.getDefaultInstance()))
                .thenIdle(3)
                .thenExecute(() -> helper.pulseRedstone(1, 1, 2, 2))
                .thenExecuteAfter(7, () -> helper.assertContainerContains(1, 2, 1, Items.REDSTONE_BLOCK)) // Should craft the redstone block

                .thenSucceed());
    }

    public static class Recipes extends RecipeProvider {
        protected Recipes(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            super(provider, recipeOutput);
        }

        @Override
        protected void buildRecipes() {
            this.shaped(RecipeCategory.MISC, Blocks.REDSTONE_BLOCK)
                    .define('#', Blocks.YELLOW_WOOL)
                    .define('X', Blocks.CHERRY_PLANKS)
                    .pattern("###")
                    .pattern("XXX")
                    .group("bed")
                    .unlockedBy(getHasName(Blocks.CHERRY_PLANKS), has(Blocks.CHERRY_PLANKS))
                    .save(output, ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath("neotests_recipe_priorities", "higher_priority_test")));
        }

        private static class Runner extends RecipeProvider.Runner {
            protected Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
                super(packOutput, registries);
            }

            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                return new RecipePrioritiesTest.Recipes(registries, output);
            }

            @Override
            public String getName() {
                return "RecipePrioritiesTest Recipes";
            }
        }
    }
}
