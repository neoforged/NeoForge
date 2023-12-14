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
}
