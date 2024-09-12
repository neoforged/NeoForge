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
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "data.feature_flags")
public class CustomFeatureFlagsPackTest {
    @TestHolder(description = "Tests that feature flag packs get shown in the experiments screen", enabledByDefault = true)
    static void testFeatureFlagPacks(final DynamicTest test) {
        String modId = test.createModId();

        test.framework().modEventBus().addListener((AddPackFindersEvent event) -> {
            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(modId, "feature_flag_test_packs/flag_test_pack"),
                    PackType.SERVER_DATA,
                    Component.literal("Custom FeatureFlag test pack"),
                    PackSource.FEATURE,
                    false,
                    Pack.Position.TOP);

            // Add 6 additional packs to visually overflow the vanilla experiments screen
            for (int i = 0; i < 6; i++) {
                event.addPackFinders(
                        ResourceLocation.fromNamespaceAndPath(modId, "feature_flag_test_packs/flag_test_pack_" + i),
                        PackType.SERVER_DATA,
                        Component.literal("Custom FeatureFlag test pack " + i),
                        PackSource.FEATURE,
                        false,
                        Pack.Position.TOP);
            }

            test.pass();
        });
    }
}
