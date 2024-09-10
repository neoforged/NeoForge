/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.deferred.DeferredItem;
import net.neoforged.neoforge.registries.deferred.DeferredItems;

/**
 * This test mod provides two items for testing the Forge onStopUsing hook. Both items attempt to create an item that increases FOV and allows creative flight when used
 * <ul>
 * <li>{@code stop_using_item:bad_scope}: Implements the item without the onStopUsing to demonstrate the problem.
 * Should see that when selecting another hotbar slot or dropping the item, the FOV is not properly reverted and you remain flying.
 * </li>
 * <li>{@code stop_using_item:good_scope}: Implements the item with onStopUsing to test that the hook hook works.
 * Should see that when selecting another hotbar slot or dropping the item, the FOV is properly reverted and you stop flying.
 * </li>
 * </ul>
 */
@Mod(MayFlyAttributeTest.MODID)
public class MayFlyAttributeTest {
    protected static final String MODID = "may_fly_attribute_item";
    private static final DeferredItems ITEMS = DeferredItems.createItems(MODID);

    private static final ResourceLocation MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MODID, "add_flight");
    private static final AttributeModifier MODIFIER = new AttributeModifier(MODIFIER_ID, 1D, AttributeModifier.Operation.ADD_VALUE);

    public MayFlyAttributeTest(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    /** Successful "scope item" using the Forge method, all cases of stopping using the item will remove the flight ability */
    public static DeferredItem<Item> GOOD = ITEMS.register("good_scope", () -> new InvertedTelescope(new Item.Properties()));

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(GOOD);
        }
    }

    private static class InvertedTelescope extends Item {
        public InvertedTelescope(Properties props) {
            super(props);
        }

        @Override
        public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
            return ItemAttributeModifiers.builder()
                    .add(NeoForgeMod.CREATIVE_FLIGHT, MODIFIER, EquipmentSlotGroup.ANY)
                    .build();
        }
    }
}
