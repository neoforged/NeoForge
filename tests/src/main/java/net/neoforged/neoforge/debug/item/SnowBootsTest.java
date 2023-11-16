/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(SnowBootsTest.MODID)
public class SnowBootsTest {
    public static final String MODID = "snow_boots_test";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static DeferredItem<Item> SNOW_BOOTS = ITEMS.register("snow_boots", () -> new ArmorItem(ArmorMaterials.DIAMOND, ArmorItem.Type.BOOTS, (new Item.Properties())) {
        @Override
        public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
            return wearer.getHealth() < 10;
        }
    });

    public SnowBootsTest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            event.accept(SNOW_BOOTS);
    }
}
