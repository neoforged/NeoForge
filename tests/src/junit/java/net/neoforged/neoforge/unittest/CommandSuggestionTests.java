/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

public class CommandSuggestionTests {
    private static final List<String> PRETEND_BLOCK_REGISTRY = List.of(
            "minecraft:sea_pickle",
            "modid1:dried_pickle",
            "minecraft:grass",
            "modid2:burnt_grass");

    @Test
    public void commandSuggestionTest_pathSearchIsNotMinecraftHardcoded() {
        List<String> suggestions = new ArrayList<>();
        SharedSuggestionProvider.filterResources(PRETEND_BLOCK_REGISTRY, "pickle", ResourceLocation::parse, suggestions::add);

        // ["minecraft:sea_pickle"] is what vanilla will do without the patch
        assertEquals(List.of("minecraft:sea_pickle", "modid1:dried_pickle"), suggestions);
    }

    @Test
    public void commandSuggestionTest_minecraftNamespaceSearchStillWorks() {
        List<String> suggestions = new ArrayList<>();
        SharedSuggestionProvider.filterResources(PRETEND_BLOCK_REGISTRY, "minecraft:", ResourceLocation::parse, suggestions::add);

        // ["minecraft:sea_pickle", "minecraft:grass"] is what vanilla will do without the patch
        assertEquals(List.of("minecraft:sea_pickle", "minecraft:grass"), suggestions);
    }

    @Test
    public void commandSuggestionTest_modNamespaceSearchStillWorks() {
        List<String> suggestions = new ArrayList<>();
        SharedSuggestionProvider.filterResources(PRETEND_BLOCK_REGISTRY, "modid", ResourceLocation::parse, suggestions::add);

        // ["modid1:dried_pickle", "modid2:burnt_grass"] is what vanilla will do without the patch
        assertEquals(List.of("modid1:dried_pickle", "modid2:burnt_grass"), suggestions);
    }
}
