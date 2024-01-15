/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = OverlayTests.GROUP)
public class OverlayTests {
    public static final String GROUP = "resources";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if pack overlays from mods work")
    static void packOverlay(final DynamicTest test) {
        var tagKey = TagKey.create(Registries.BLOCK, new ResourceLocation("pack_overlays_test", "must_be_overlayed"));
        test.onGameTest(helper -> {
            helper.assertTrue(Blocks.DIAMOND_BLOCK.defaultBlockState().is(tagKey), "Overlay was not applied");
            helper.assertFalse(Blocks.COBBLESTONE.defaultBlockState().is(tagKey), "File under overlay was applied");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if conditions work for pack overlays")
    static void conditionalOverlay(final DynamicTest test) {
        var enabledKey = TagKey.create(Registries.BLOCK, new ResourceLocation("conditional_overlays_test", "overlay_enabled"));
        var disabledKey = TagKey.create(Registries.BLOCK, new ResourceLocation("conditional_overlays_test", "overlay_disabled"));
        test.onGameTest(helper -> {
            helper.assertTrue(Blocks.DIAMOND_BLOCK.defaultBlockState().is(enabledKey), "Enabled overlay was not applied");
            helper.assertFalse(Blocks.COBBLESTONE.defaultBlockState().is(enabledKey), "File under enabled overlay was applied");
            helper.assertFalse(Blocks.DIAMOND_BLOCK.defaultBlockState().is(disabledKey), "Disabled overlay was applied");
            helper.assertTrue(Blocks.COBBLESTONE.defaultBlockState().is(disabledKey), "File under disabled overlay was not applied");
            helper.succeed();
        });
    }
}
