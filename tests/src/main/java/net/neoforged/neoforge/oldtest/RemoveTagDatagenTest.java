/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(RemoveTagDatagenTest.MODID)
public class RemoveTagDatagenTest {
    public static final String MODID = "remove_tag_datagen_test";
    public static final TagKey<Block> TEST_TAG_BLOCK = BlockTags.create(Identifier.withDefaultNamespace("test_tag"));
    public static final TagKey<Item> TEST_TAG_ITEM = ItemTags.create(Identifier.withDefaultNamespace("test_tag"));

    public RemoveTagDatagenTest(IEventBus modBus) {
        modBus.addListener(this::onGatherData);
    }

    private void onGatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();

        var blocks = new BlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), MODID) {
            @SuppressWarnings("unchecked")
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                this.tag(TEST_TAG_BLOCK)
                        .remove(BlockItemIds.DIRT.block())
                        .remove(BlockItemIds.OAK_DOOR.block(), BlockItemIds.DARK_OAK_DOOR.block())
                        .remove(BlockItemIds.ANVIL.block())
                        .remove(BlockItemIds.BASALT.block(), BlockItemIds.POLISHED_ANDESITE.block())
                        .remove(BlockTags.BEEHIVES)
                        .remove(BlockTags.BANNERS, BlockTags.BEDS);
            }
        };

        generator.addProvider(true, blocks);

        generator.addProvider(true, new BlockTagCopyingItemTagProvider(generator.getPackOutput(), event.getLookupProvider(), blocks.contentsGetter(), MODID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // This is for testing if it is functional, remove spruce_planks from planks, which makes us unable to craft beds with them.
                this.tag(ItemTags.PLANKS).remove(BlockItemIds.SPRUCE_PLANKS.item());
                // This is for testing deep values, removing a entry in a tag that is referenced by another tag
                // Remove GOLD_ORE from the PIGLIN_LOVED tag, which is added by PIGLIN_LOVED reference to the GOLD_ORES tag
                // This will make GOLD_ORE unable to be loved by piglins.
                this.tag(ItemTags.PIGLIN_LOVED).remove(BlockItemIds.GOLD_ORE.item());

                this.copy(TEST_TAG_BLOCK, TEST_TAG_ITEM);
            }
        });
    }
}
