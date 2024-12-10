/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(RemoveTagDatagenTest.MODID)
public class RemoveTagDatagenTest {
    public static final String MODID = "remove_tag_datagen_test";
    public static final TagKey<Block> TEST_TAG = BlockTags.create(ResourceLocation.withDefaultNamespace("test_tag"));

    public RemoveTagDatagenTest(IEventBus modBus) {
        modBus.addListener(this::onGatherData);
    }

    private void onGatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();

        var blocks = new BlockTagsProvider(generator.getPackOutput(), event.getLookupProvider(), MODID, helper) {
            @SuppressWarnings("unchecked")
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                this.tag(TEST_TAG)
                        .remove(key(Blocks.DIRT))
                        .remove(key(Blocks.OAK_DOOR), key(Blocks.DARK_OAK_DOOR))
                        .remove(key(Blocks.ANVIL))
                        .remove(key(Blocks.BASALT), key(Blocks.POLISHED_ANDESITE))
                        .remove(BlockTags.BEEHIVES)
                        .remove(BlockTags.BANNERS, BlockTags.BEDS);
            }
        };

        generator.addProvider(true, blocks);

        generator.addProvider(true, new ItemTagsProvider(generator.getPackOutput(), event.getLookupProvider(), blocks.contentsGetter(), MODID, helper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                // This is for testing if it is functional, remove spruce_planks from planks, which makes us unable to craft beds with them.
                this.tag(ItemTags.PLANKS).remove(key(Blocks.SPRUCE_PLANKS));
                // This is for testing deep values, removing a entry in a tag that is referenced by another tag
                // Remove GOLD_ORE from the PIGLIN_LOVED tag, which is added by PIGLIN_LOVED reference to the GOLD_ORES tag
                // This will make GOLD_ORE unable to be loved by piglins.
                this.tag(ItemTags.PIGLIN_LOVED).remove(key(Items.GOLD_ORE));
            }
        });
    }

    private static ResourceLocation key(Block value) {
        return BuiltInRegistries.BLOCK.getKey(value);
    }

    private static ResourceLocation key(Item value) {
        return BuiltInRegistries.ITEM.getKey(value);
    }
}
