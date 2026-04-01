/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.data.event.ModifyJsonDataEvent;
import net.neoforged.neoforge.debug.block.BlockTests;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = { BlockTests.GROUP + ".event", "event" })
public class ModifyJsonDataEventTest {
    @TestHolder(description = "Tests if the ModifyJsonDataEvent exposes a mutable map of JSON data.")
    public static void mutableMapEvent(final DynamicTest test) {
        test.framework().modEventBus().addListener((final ModifyJsonDataEvent event) -> {
            // Ensure the event is fired when recipes are loaded.
            if (event.getPrefix().equals("recipe")) {
                // Grab the map of JSON data from the event.
                Map<Identifier, JsonElement> jsons = event.getJsonData();
                // Ensure the map is mutable.
                try {
                    jsons.put(Identifier.withDefaultNamespace("test"), jsons.values().iterator().next());
                } catch (UnsupportedOperationException e) {
                    test.fail("Map is not mutable");
                    return;
                }
                // Ensure the event exposes registry access
                try {
                    event.lookupOrThrow(Registries.BIOME).getter().getOrThrow(Biomes.PLAINS);
                } catch (NoSuchElementException e) {
                    test.fail("Registry lookup failed");
                }
            }
            test.pass();
        });
    }
}
