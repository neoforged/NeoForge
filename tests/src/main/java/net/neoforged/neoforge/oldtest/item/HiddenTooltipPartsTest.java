/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HiddenTooltipPartsTest.MOD_ID)
public class HiddenTooltipPartsTest {
    public static final String MOD_ID = "hidden_tooltip_parts";
    public static final boolean ENABLED = true;
    private static final AttributeModifier MODIFIER = new AttributeModifier(MOD_ID, 10f, Operation.ADD_VALUE);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredItem<Item> TEST_ITEM = ITEMS.register("test_item", () -> new TestItem(new Item.Properties()));

    public HiddenTooltipPartsTest(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(TEST_ITEM);
    }

    static class TestItem extends Item {
        public TestItem(Properties properties) {
            super(properties);
        }

        @Override
        public ItemAttributeModifiers getAttributeModifiers(ItemStack stack) {
            return ItemAttributeModifiers.builder()
                    .add(Attributes.ARMOR, MODIFIER, EquipmentSlotGroup.ANY)
                    .build();
        }

        // TODO PORTING 1.20.5 - fix or remove test
//        @Override
//        public int getDefaultTooltipHideFlags(ItemStack stack) {
//            return ItemStack.TooltipPart.MODIFIERS.getMask();
//        }
    }
}
