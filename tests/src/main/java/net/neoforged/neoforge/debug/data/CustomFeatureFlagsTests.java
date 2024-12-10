/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "data.feature_flags")
public class CustomFeatureFlagsTests {
    @TestHolder(description = "Tests that feature flag packs get shown in the experiments screen", enabledByDefault = true)
    static void testFeatureFlagPacks(final DynamicTest test) {
        test.framework().modEventBus().addListener((AddPackFindersEvent event) -> {
            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath("neotests", "feature_flag_test_packs/flag_test_pack"),
                    PackType.SERVER_DATA,
                    Component.literal("Custom FeatureFlag test pack"),
                    PackSource.FEATURE,
                    false,
                    Pack.Position.TOP);

            // Add 6 additional packs to visually overflow the vanilla experiments screen
            for (int i = 0; i < 6; i++) {
                event.addPackFinders(
                        ResourceLocation.fromNamespaceAndPath("neotests", "feature_flag_test_packs/flag_test_pack_" + i),
                        PackType.SERVER_DATA,
                        Component.literal("Custom FeatureFlag test pack " + i),
                        PackSource.FEATURE,
                        false,
                        Pack.Position.TOP);
            }

            test.pass();
        });
    }

    @TestHolder(description = "Verifies that registered objects using a custom feature flag are not accessible without the feature flag being enabled", enabledByDefault = true)
    static void testFeatureGating(final DynamicTest test) {
        test.framework().modEventBus().addListener((AddPackFindersEvent event) -> event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath("neotests", "feature_flag_test_packs/gating_test_pack"),
                PackType.SERVER_DATA,
                Component.literal("Custom FeatureFlag gating test pack"),
                PackSource.FEATURE,
                true,
                Pack.Position.TOP));

        FeatureFlag baseRangeEnabledTestFlag = FeatureFlags.REGISTRY.getFlag(ResourceLocation.fromNamespaceAndPath("custom_feature_flags_pack_test", "many_flags_9"));
        FeatureFlag baseRangeDisabledTestFlag = FeatureFlags.REGISTRY.getFlag(ResourceLocation.fromNamespaceAndPath("custom_feature_flags_pack_test", "many_flags_10"));
        FeatureFlag extRangeEnabledTestFlag = FeatureFlags.REGISTRY.getFlag(ResourceLocation.fromNamespaceAndPath("custom_feature_flags_pack_test", "many_flags_99"));
        FeatureFlag extRangeDisabledTestFlag = FeatureFlags.REGISTRY.getFlag(ResourceLocation.fromNamespaceAndPath("custom_feature_flags_pack_test", "many_flags_100"));

        DeferredItem<Item> baseRangeEnabledTestItem = test.registrationHelper().items()
                .registerSimpleItem("base_range_enabled_test", new Item.Properties().requiredFeatures(baseRangeEnabledTestFlag));
        DeferredItem<Item> baseRangeDisabledTestItem = test.registrationHelper().items()
                .registerSimpleItem("base_range_disabled_test", new Item.Properties().requiredFeatures(baseRangeDisabledTestFlag));
        DeferredItem<Item> extRangeEnabledTestItem = test.registrationHelper().items()
                .registerSimpleItem("ext_range_enabled_test", new Item.Properties().requiredFeatures(extRangeEnabledTestFlag));
        DeferredItem<Item> extRangeDisabledTestItem = test.registrationHelper().items()
                .registerSimpleItem("ext_range_disabled_test", new Item.Properties().requiredFeatures(extRangeDisabledTestFlag));

        test.eventListeners().forge().addListener((ServerStartedEvent event) -> {
            FeatureFlagSet flagSet = event.getServer().getLevel(Level.OVERWORLD).enabledFeatures();
            if (!baseRangeEnabledTestItem.get().isEnabled(flagSet)) {
                test.fail("Item with enabled custom flag in base mask range was unexpectedly disabled");
            } else if (baseRangeDisabledTestItem.get().isEnabled(flagSet)) {
                test.fail("Item with disabled custom flag in base mask range was unexpectedly enabled");
            } else if (!extRangeEnabledTestItem.get().isEnabled(flagSet)) {
                test.fail("Item with enabled custom flag in extended mask range was unexpectedly disabled");
            } else if (extRangeDisabledTestItem.get().isEnabled(flagSet)) {
                test.fail("Item with disabled custom flag in extended mask range was unexpectedly enabled");
            } else {
                test.pass();
            }
        });
    }

    @TestHolder(description = "Tests that elements can be toggled via conditions using the flag condition", enabledByDefault = true)
    static void testFlagCondition(DynamicTest test, RegistrationHelper reg) {
        // custom flag are provided by our other flag tests
        // and enabled via our `custom featureflag test pack`
        var flagName = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_pack_test", "test_flag");
        var flag = FeatureFlags.REGISTRY.getFlag(flagName);

        var modId = reg.modId();
        var enabledRecipeName = ResourceKey.create(Registries.RECIPE, ResourceLocation.fromNamespaceAndPath(modId, "diamonds_from_dirt"));

        reg.addClientProvider(event -> new RecipeProvider.Runner(event.getGenerator().getPackOutput(), event.getLookupProvider()) {
            @Override
            protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
                class Provider extends RecipeProvider implements IConditionBuilder {
                    protected Provider(HolderLookup.Provider p_360573_, RecipeOutput p_360872_) {
                        super(p_360573_, p_360872_);
                    }

                    @Override
                    protected void buildRecipes() {
                        // recipe available when above flag is enabled
                        shapeless(RecipeCategory.MISC, Items.DIAMOND)
                                .requires(ItemTags.DIRT)
                                .unlockedBy("has_dirt", has(ItemTags.DIRT))
                                .save(output.withConditions(featureFlagsEnabled(flag)), enabledRecipeName);
                    }
                }
                return new Provider(registries, output);
            }

            @Override
            public String getName() {
                return "conditional_flag_recipes";
            }
        });

        test.eventListeners().forge().addListener((ServerStartedEvent event) -> {
            var server = event.getServer();
            var isFlagEnabled = server.getWorldData().enabledFeatures().contains(flag);
            var recipeMap = server.getRecipeManager().recipeMap();
            var hasEnabledRecipe = recipeMap.byKey(enabledRecipeName) != null;

            if (isFlagEnabled) {
                if (!hasEnabledRecipe) {
                    test.fail("Missing recipe '" + enabledRecipeName.location() + "', This should be enabled due to our flag '" + flagName + "' being enabled");
                }
            } else {
                if (hasEnabledRecipe) {
                    test.fail("Found recipe '" + enabledRecipeName.location() + "', This should be disabled due to our flag '" + flagName + "' being enabled");
                }
            }

            test.pass();
        });
    }
}
