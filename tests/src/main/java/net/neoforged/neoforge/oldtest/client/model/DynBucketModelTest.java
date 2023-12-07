/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.model;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DynBucketModelTest.MODID)
public class DynBucketModelTest {
    public static final boolean ENABLE = false; // TODO fix
    public static final String MODID = "dyn_bucket_model_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> DRIP_BUCKET = ITEMS.register("drip_bucket", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> LAVA_OBSIDIAN = ITEMS.register("lava_obsidian", () -> new Item(new Item.Properties()));

    public DynBucketModelTest() {
        if (ENABLE) {
            IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            ITEMS.register(modEventBus);
            modEventBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(DRIP_BUCKET);
            event.accept(LAVA_OBSIDIAN);
        }
    }
}
