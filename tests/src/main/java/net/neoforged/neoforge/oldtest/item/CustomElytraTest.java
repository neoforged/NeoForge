/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CustomElytraTest.MOD_ID)
public class CustomElytraTest {
    public static final String MOD_ID = "custom_elytra_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredItem<Item> TEST_ELYTRA = ITEMS.registerItem(
            "test_elytra", props -> new Item(props.durability(100)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setModel(ResourceLocation.fromNamespaceAndPath(MOD_ID, "test_elytra"))
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                            .setDamageOnHurt(false)
                            .build())));

    public CustomElytraTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(TEST_ELYTRA);
    }
}
