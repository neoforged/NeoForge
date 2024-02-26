/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ConfigFlagItemTest.ID)
public final class ConfigFlagItemTest {
    public static final String ID = "config_flag_item_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);

    public static final DeferredItem<ConfigItem> CONFIG_ITEM = ITEMS.registerItem("config_item", ConfigItem::new);
    public static final DeferredBlock<ConfigBlock> CONFIG_BLOCK = BLOCKS.registerBlock("config_block", ConfigBlock::new, BlockBehaviour.Properties.ofLegacyCopy(Blocks.STONE));

    public static final ModConfigSpec.BooleanValue ENABLE_CONFIG_ITEM;
    public static final ModConfigSpec.BooleanValue ENABLE_CONFIG_BLOCK;

    static {
        var builder = new ModConfigSpec.Builder();

        Function<ResourceLocation, ModConfigSpec.BooleanValue> config = id -> builder
                .comment("Enable the Item: '%s'".formatted(id))
                .define("enable.%s".formatted(id.getPath()), false);

        ENABLE_CONFIG_ITEM = config.apply(CONFIG_ITEM.getId());
        ENABLE_CONFIG_BLOCK = config.apply(CONFIG_BLOCK.getId());

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }

    public ConfigFlagItemTest(IEventBus bus) {
        BLOCKS.getEntries().forEach(ITEMS::registerSimpleBlockItem);

        ITEMS.register(bus);
        BLOCKS.register(bus);

        bus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(CONFIG_ITEM);
                event.accept(CONFIG_BLOCK);
            }
        });
    }

    public static final class ConfigItem extends Item {
        public ConfigItem(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isDisabled() {
            return !ENABLE_CONFIG_ITEM.get();
        }
    }

    public static final class ConfigBlock extends Block {
        public ConfigBlock(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isDisabled() {
            return !ENABLE_CONFIG_BLOCK.get();
        }
    }
}
