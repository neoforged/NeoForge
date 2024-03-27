/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

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
import net.neoforged.neoforge.common.extensions.IFeatureElementExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Current known issues
 * <p>
 * When playing on a server elements do not sync their enabled states correctly
 * <ul>
 * <li>
 * Since server configs are currently only synced during player login.<br>
 * If the config changes live while a client is connected to a server elements will
 * remain in their previous enabled state for that client until they relog or server restarts<br>
 * '/reload' does not fix this issue
 * </li>
 * </ul>
 */
@Mod(ConfigFlagItemTest.ID)
public final class ConfigFlagItemTest {
    public static final String ID = "config_flag_item_test";

    public static final ModConfigSpec.BooleanValue ENABLE_CONFIG_ITEM;
    public static final ModConfigSpec.BooleanValue ENABLE_CONFIG_BLOCK;

    static {
        var builder = new ModConfigSpec.Builder();

        ENABLE_CONFIG_ITEM = builder.comment("Enable the Item: '%s:config_item'".formatted(ID))
                .define("enable.config_item", true);

        ENABLE_CONFIG_BLOCK = builder.comment("Enable the Block: '%s:config_block'".formatted(ID))
                .define("enable.config_block", true);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, builder.build());
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);

    // the following elements are always registered to the game
    // but are marked as only being enabled via our configs using the new 'isFeatureEnabled' hooks
    // telling the game to only enable them when the config returns 'true'
    public static final DeferredItem<Item> CONFIG_ITEM = ITEMS.registerSimpleItem("config_item", new Item.Properties()
            .isFeatureEnabled(ENABLE_CONFIG_ITEM));

    public static final DeferredBlock<Block> CONFIG_BLOCK = BLOCKS.registerSimpleBlock("config_block", BlockBehaviour.Properties
            .ofLegacyCopy(Blocks.STONE)
            .isFeatureEnabled(ENABLE_CONFIG_BLOCK));

    // this block (and its associated item) should be registered to the game
    // but always be disabled, never show up in creative mode tabs, be givable or placeable
    public static final DeferredBlock<Block> ALWAYS_DISABLED_BLOCK = BLOCKS.registerSimpleBlock("always_disabled", BlockBehaviour.Properties
            .ofLegacyCopy(Blocks.STONE)
            .isFeatureEnabled(IFeatureElementExtension::never));

    public ConfigFlagItemTest(IEventBus bus) {
        // take note on how we register a bare minimum block item for our blocks
        // this item will still be marked as disabled via the config
        // as BlockItem delegates its 'isFeatureEnabled' to the associated block
        // this logic should also be true for 'SpawnEgg -> EntityType' bindings
        BLOCKS.getEntries().forEach(ITEMS::registerSimpleBlockItem);

        ITEMS.register(bus);
        BLOCKS.register(bus);

        // add our elements to creative mode tabs
        // while we could be checking if the items are enabled
        // the internal creative mode tab logic already does this for us
        // so no need to check enabled state here
        //
        // disabled items are not visible in creative mode tabs
        bus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                event.accept(CONFIG_ITEM);
                event.accept(CONFIG_BLOCK);
            }
        });
    }
}
