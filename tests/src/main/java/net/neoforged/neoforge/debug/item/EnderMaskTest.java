/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod(EnderMaskTest.MODID)
public class EnderMaskTest {
    public static final String MODID = "ender_mask_test";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static RegistryObject<Item> ENDER_MASK = ITEMS.register("ender_mask", () -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, (new Item.Properties())) {
        @Override
        public boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
            return player.experienceLevel > 10;
        }
    });

    public EnderMaskTest() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(ENDER_MASK);
    }
}
