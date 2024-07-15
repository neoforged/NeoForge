/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.flag.FlagProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "modded_feature_flags")
public interface FlagTests {
    @TestHolder(description = "Registers custom modded flag and item which requires it")
    static void test(DynamicTest test) {
        var registration = test.registrationHelper();
        var modId = test.createModId();
        var testFlag = ResourceLocation.fromNamespaceAndPath(modId, "test_flag");

        registration.addProvider(event -> new FlagProvider(event.getGenerator().getPackOutput(), modId, event.getLookupProvider()) {
            @Override
            protected void generate() {
                flag(testFlag);
            }
        });

        registration.items().register("flagged_item", () -> new Item(new Item.Properties()
                .requiredFlags(testFlag)));
    }
}
