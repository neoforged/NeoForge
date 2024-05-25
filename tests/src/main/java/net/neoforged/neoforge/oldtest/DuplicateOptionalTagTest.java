/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tests that the values for defaulted optional tags defined in multiple places are combined.
 *
 * <p>The optional tag defined by this mod is deliberately not defined in a data pack, to cause it to 'default' and
 * trigger the behavior being tested.</p>
 *
 * @see <a href="https://github.com/MinecraftForge/MinecraftForge/issues/7570">MinecraftForge/MinecraftForge#7570</a>
 */
@Mod(DuplicateOptionalTagTest.MODID)
public class DuplicateOptionalTagTest {
    private static final Logger LOGGER = LogManager.getLogger();

    static final String MODID = "duplicate_optional_tag_test";
    private static final ResourceLocation TAG_NAME = ResourceLocation.fromNamespaceAndPath(MODID, "test_optional_tag");

    private static final Set<Block> TAG_A_DEFAULTS = Set.of(Blocks.BEDROCK);
    private static final Set<Block> TAG_B_DEFAULTS = Set.of(Blocks.WHITE_WOOL);

    private static final TagKey<Block> TAG_A = BlockTags.create(TAG_NAME);
    private static final TagKey<Block> TAG_B = BlockTags.create(TAG_NAME);

    public DuplicateOptionalTagTest() {
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void onServerStarted(ServerStartedEvent event) {
        Set<Block> tagAValues = BuiltInRegistries.BLOCK.getTag(TAG_A).map(tag -> tag.stream().map(Holder::value).collect(Collectors.toUnmodifiableSet())).orElse(TAG_A_DEFAULTS);
        Set<Block> tagBValues = BuiltInRegistries.BLOCK.getTag(TAG_B).map(tag -> tag.stream().map(Holder::value).collect(Collectors.toUnmodifiableSet())).orElse(TAG_B_DEFAULTS);

        if (!tagAValues.equals(tagBValues)) {
            LOGGER.error("Values of both optional tag instances are not the same: first instance: {}, second instance: {}", tagAValues, tagBValues);
            return;
        }

        final Set<Block> expected = Sets.union(TAG_A_DEFAULTS, TAG_B_DEFAULTS).stream().collect(Collectors.toUnmodifiableSet());
        if (!tagAValues.equals(expected)) {
            IllegalStateException e = new IllegalStateException("Optional tag values do not match!");
            LOGGER.error("Values of the optional tag do not match the expected union of their defaults: expected {}, got {}", expected, tagAValues, e);
            return;
        }

        LOGGER.info("Optional tag instances match each other and the expected union of their defaults");
    }
}
