/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// TODO: Remove FeatureFlags.MOD_EXPERIMENTAL
@Mod(ExperimentalItemTest.ID)
public final class ExperimentalItemTest {
    public static final String ID = "experimental_item_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    // the following item/block(s) should only be enabled when forges experimental feature flag is enabled
    // they are still registered to the game but completely unusable/obtainable by any means
    // enable the feature flag 'neoforge:mod_experimental' to toggle these on
    // this can be done via the experimental selection screen when creating a new world
    public static final DeferredItem<Item> EXPERIMENTAL_ITEM = ITEMS.registerSimpleItem("experimental_item", new Item.Properties().requiredFeatures(FeatureFlags.MOD_EXPERIMENTAL));
    public static final DeferredBlock<Block> EXPERIMENTAL_BLOCK = BLOCKS.registerBlock("experimental_block", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).requiredFeatures(FeatureFlags.MOD_EXPERIMENTAL));
    public static final DeferredItem<BlockItem> EXPERIMENTAL_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(EXPERIMENTAL_BLOCK);
    public static final DeferredItem<ConfigItem> CONFIG_ITEM = ITEMS.registerItem("config_item", ConfigItem::new);

    public static final ModConfigSpec.BooleanValue ENABLE_EXP_ITEM;

    static {
        var builder = new ModConfigSpec.Builder();
        ENABLE_EXP_ITEM = builder
                .comment("Enable the experimental item: '%s'".formatted(CONFIG_ITEM.getId()))
                .define("enable.%s".formatted(CONFIG_ITEM.getId().getPath()), false);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }

    public ExperimentalItemTest(IEventBus bus) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
    }

    public static final class ConfigItem extends Item {
        public ConfigItem(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isDisabled() {
            return !ENABLE_EXP_ITEM.get();
        }
    }
}
