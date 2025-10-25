/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "debug_entry_tests", side = Dist.CLIENT)
public interface DebugEntryTests {
    @TestHolder(description = "Registers a new custom DebugScreenEntry", enabledByDefault = true)
    static void testCustomEntry(DynamicTest test) {
        var modId = test.createModId();
        var id = ResourceLocation.fromNamespaceAndPath(modId, "debug_screen_entry");

        var modBus = test.eventListeners().mod();

        modBus.addListener((RegisterDebugEntriesEvent event) -> {
            event.register(id, (displayer, level, clientChunk, serverChunk) -> displayer.addLine("Test Debug Screen Entry!!!!"));
            event.includeInProfile(id, DebugScreenProfile.DEFAULT, DebugScreenEntryStatus.ALWAYS_ON);

            class CustomCategoryNoop extends DebugEntryNoop {
                // SCREEN_TEXT -> 1
                // RENDERER -> 2
                // we use 1.5 to be sorted between 'SCREEN_TEXT' and 'RENDERER'
                private static final DebugEntryCategory CUSTOM_CATEGORY = new DebugEntryCategory(Component.literal("Custom Category"), 1.5F);

                @Override
                public DebugEntryCategory category() {
                    return CUSTOM_CATEGORY;
                }
            }

            // register bunch of dummy entries to test sorting
            // notice the order of these entries (paths)
            // 'b' is registered before 'a'
            // but due to sorting in game 'a' should come first
            event.register(ResourceLocation.fromNamespaceAndPath(modId, "dummy_entry_b"), new CustomCategoryNoop());
            event.register(ResourceLocation.fromNamespaceAndPath(modId, "dummy_entry_a"), new CustomCategoryNoop());
            event.register(ResourceLocation.fromNamespaceAndPath("dummy0", "dummy_entry_b"), new CustomCategoryNoop());
            event.register(ResourceLocation.fromNamespaceAndPath("dummy0", "dummy_entry_a"), new CustomCategoryNoop());

            event.register(ResourceLocation.fromNamespaceAndPath("dummy1", "dummy_entry_b"), new DebugEntryNoop());
            event.register(ResourceLocation.fromNamespaceAndPath("dummy1", "dummy_entry_a"), new DebugEntryNoop());

            test.pass();
        });

        modBus.addListener((FMLLoadCompleteEvent event) -> {
            if (test.status() != Test.Status.PASSED)
                test.fail("Game loaded but RegisterDebugEntriesEvent was never received!");
        });
    }
}
