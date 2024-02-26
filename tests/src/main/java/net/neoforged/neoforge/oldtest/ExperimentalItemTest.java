/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(ExperimentalItemTest.ID)
public final class ExperimentalItemTest {
    public static final String ID = "experimental_item_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
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
