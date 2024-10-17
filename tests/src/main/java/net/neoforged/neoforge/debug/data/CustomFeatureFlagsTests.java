/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

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
}
