/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.apache.commons.lang3.function.TriConsumer;

@ForEachTest
public final class ExperimentalTests {
    @TestHolder(value = "experimental_tests_base", description = "Test providing experimental items and blocks")
    private static void baseMod(DynamicTest test) {
        var registration = test.registrationHelper();
        var items = registration.items();

        items.registerSimpleItem("experimental_item", new Item.Properties().requiredFeatures(FeatureFlags.MOD_EXPERIMENTAL));

        var block = registration.blocks().registerSimpleBlock("experimental_block", BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).requiredFeatures(FeatureFlags.MOD_EXPERIMENTAL));
        items.registerSimpleBlockItem(block);
    }

    @TestHolder(value = "experimental_tests_moda", description = "Test providing experimental feature pack (dirt -> diamond recipe)")
    private static void modA(DynamicTest test) {
        commonMod(test, (event, pack, lookupProvider) -> pack.addProvider(output -> new RecipeProvider(output, lookupProvider) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                // recipe for dirt -> diamond enabled when MOD_EXPERIMENTAL is enabled
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.DIAMOND)
                        .requires(Items.DIRT)
                        .unlockedBy("has_dirt", has(Items.DIRT))
                        .group("experimental")
                        .save(output, ResourceLocation.fromNamespaceAndPath(test.createModId(), "diamond_from_dirt"));
            }
        }));
    }

    @TestHolder(value = "experimental_tests_modb", description = "Test providing experimental feature pack (diamond -> dirt recipe)")
    private static void modB(DynamicTest test) {
        commonMod(test, (event, pack, lookupProvider) -> pack.addProvider(output -> new RecipeProvider(output, lookupProvider) {
            @Override
            protected void buildRecipes(RecipeOutput output) {
                // recipe for diamond -> dirt enabled when MOD_EXPERIMENTAL is enabled
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.DIRT)
                        .requires(Tags.Items.GEMS_DIAMOND)
                        .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
                        .group("experimental")
                        .save(output, ResourceLocation.fromNamespaceAndPath(test.createModId(), "dirt_from_diamond"));
            }
        }));
    }

    private static void commonMod(DynamicTest test, TriConsumer<DataGenerator, DataGenerator.PackGenerator, CompletableFuture<HolderLookup.Provider>> gatherData) {
        var packName = "experimental_features";
        var modBus = test.framework().modEventBus();
        var modId = test.createModId();

        // register pack finder for experimental features pack
        modBus.addListener(AddPackFindersEvent.class, event -> event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath("neotests", "data/" + modId + "/datapacks/" + packName),
                PackType.SERVER_DATA,
                Component.literal("Experimental Features (" + modId + ")"),
                PackSource.create(UnaryOperator.identity(), false),
                false,
                Pack.Position.BOTTOM));

        modBus.addListener(GatherDataEvent.class, event -> {
            var generator = event.getGenerator();
            var pack = generator.getBuiltinDatapack(event.includeServer(), modId, packName);
            // generate pack metadata for experimental features pack
            pack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.literal("Enables experimental features (" + modId + ")"), FeatureFlagSet.of(FeatureFlags.MOD_EXPERIMENTAL)));
            // generate any additional data for this pack
            gatherData.accept(generator, pack, event.getLookupProvider());
        });
    }
}
