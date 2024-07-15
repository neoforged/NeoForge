/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.flag.FlagProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "modded_feature_flags")
public interface FlagTests {
    @TestHolder(description = "Generates custom modded feature flag")
    static void test(DynamicTest test) {
        var registration = test.registrationHelper();
        var modId = test.createModId();

        registration.addProvider(event -> new FlagProvider(event.getGenerator().getPackOutput(), modId, event.getLookupProvider()) {
            @Override
            protected void generate() {
                flag(ResourceLocation.fromNamespaceAndPath(modId, "enabled_by_default"), true);
                flag(ResourceLocation.fromNamespaceAndPath(modId, "disabled"));
            }
        });
    }
}
