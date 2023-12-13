/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CustomMobBucketTest.MODID)
public class CustomMobBucketTest {
    public static final String MODID = "custom_mob_bucket_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final boolean ENABLED = true;

    public static final DeferredItem<Item> COW_BUCKET = ITEMS.register("cow_bucket", () -> new MobBucketItem(
            () -> EntityType.COW,
            () -> Fluids.WATER,
            () -> SoundEvents.BUCKET_EMPTY_FISH,
            (new Item.Properties()).stacksTo(1)));

    public CustomMobBucketTest(IEventBus modEventBus) {
        if (ENABLED) {
            ITEMS.register(modEventBus);
            modEventBus.addListener(this::addCreative);
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(COW_BUCKET);
    }
}
