/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.tags.BlockItemTagsProvider;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(RemoveTagDatagenTest.MODID)
public class RemoveTagDatagenTest {
    public static final String MODID = "remove_tag_datagen_test";
    public static final BlockItemTagId TEST_TAG = BlockItemTagId.create("test_tag");

    public RemoveTagDatagenTest(IEventBus modBus) {
        modBus.addListener(this::onGatherData);
    }

    private void onGatherData(GatherDataEvent.Client event) {
        class BlockItems extends BlockItemTagsProvider {
            protected BlockItems(Function<BlockItemTagId, CombinedAppender> tagSupplier) {
                super(tagSupplier);
            }

            @Override
            protected void run() {
                this.tag(TEST_TAG)
                        .remove(BlockItemIds.DIRT)
                        .remove(BlockItemIds.OAK_DOOR, BlockItemIds.DARK_OAK_DOOR)
                        .remove(BlockItemIds.ANVIL)
                        .remove(BlockItemIds.BASALT, BlockItemIds.POLISHED_ANDESITE)
                        .removeTag(BlockItemTags.BEDS);
            }
        }

        var blocks = event.addProvider((output, registries) -> new BlockTagsProvider(output, registries, MODID) {
            @SuppressWarnings("unchecked")
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                new BlockItems(tagId -> BlockItemTagsProvider.wrapForBlocks(this.tag(tagId.block()))).run();

                this.tag(TEST_TAG.block())
                        .remove(BlockTags.BEEHIVES, BlockTags.BANNERS);
            }
        });

        event.addProvider((output, registries) -> new ItemTagsProvider(output, registries, MODID) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
                new BlockItems(tagId -> BlockItemTagsProvider.wrapForItems(this.tag(tagId.item()))).run();

                // This is for testing if it is functional, remove spruce_planks from planks, which makes us unable to craft beds with them.
                this.tag(ItemTags.PLANKS).remove(BlockItemIds.SPRUCE_PLANKS.item());
                // This is for testing deep values, removing a entry in a tag that is referenced by another tag
                // Remove GOLD_ORE from the PIGLIN_LOVED tag, which is added by PIGLIN_LOVED reference to the GOLD_ORES tag
                // This will make GOLD_ORE unable to be loved by piglins.
                this.tag(ItemTags.PIGLIN_LOVED).remove(BlockItemIds.GOLD_ORE.item());
            }
        });
    }
}
