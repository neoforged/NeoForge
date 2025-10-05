/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
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

            test.pass();
        });

        modBus.addListener((FMLLoadCompleteEvent event) -> {
            if (test.status() != Test.Status.PASSED)
                test.fail("Game loaded but RegisterDebugEntriesEvent was never received!");
        });
    }
}
