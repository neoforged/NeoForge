/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.DeferredItems;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "capabilities.iteminventory")
public class ItemInventoryTests {
    public static final int SLOTS = 128;
    public static final int STICK_SLOT = 64;

    private static final RegistrationHelper HELPER = RegistrationHelper.create("item_inventory_tests");

    private static final DeferredItems ITEMS = HELPER.items();
    private static final DeferredItem<Item> BACKPACK;

    static {
        NonNullList<ItemStack> defaultContents = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        defaultContents.set(STICK_SLOT, Items.STICK.getDefaultInstance().copyWithCount(64));
        BACKPACK = ITEMS.register("test_backpack", () -> new Item(new Item.Properties().component(DataComponents.CONTAINER, ItemContainerContents.fromItems(defaultContents))));
    }

    @OnInit
    static void init(final TestFramework framework) {
        ITEMS.register(framework.modEventBus());
        framework.modEventBus().<RegisterCapabilitiesEvent>addListener(e -> {
            e.registerItem(ItemHandler.ITEM, (stack, ctx) -> {
                return new ComponentItemHandler(stack, DataComponents.CONTAINER, SLOTS);
            }, BACKPACK);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ComponentItemHandler can read and write from a data component")
    public static void testItemInventory(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            ItemStack stack = BACKPACK.toStack();
            IItemHandler items = stack.getCapability(ItemHandler.ITEM);

            ItemStack storedStick = items.getStackInSlot(STICK_SLOT);
            helper.assertValueEqual(storedStick.getItem(), Items.STICK, "Default contents should contain a stick at slot " + STICK_SLOT);

            ItemStack toInsert = Items.APPLE.getDefaultInstance().copyWithCount(32);
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

            ItemStack remainder = items.insertItem(STICK_SLOT, toInsert, false);
            helper.assertTrue(ItemStack.isSameItemSameComponents(toInsert, remainder), "Inserting an item where it does not fit should return the original item.");
            // Check identity equality to assert that the component object was not updated at all, even to an equivalent form.
            helper.assertTrue(contents == stack.get(DataComponents.CONTAINER), "Inserting an item where it does not fit should not change the component.");

            remainder = items.insertItem(0, toInsert, false);
            helper.assertTrue(remainder.isEmpty(), "Successfully inserting the entire item should return an empty stack.");
            helper.assertTrue(ItemStack.isSameItemSameComponents(toInsert, items.getStackInSlot(0)), "Successfully inserting an item should be visible via getStackInSlot");

            ItemContainerContents newContents = stack.get(DataComponents.CONTAINER);
            helper.assertTrue(ItemStack.isSameItemSameComponents(toInsert, newContents.getStackInSlot(0)), "Successfully inserting an item should trigger a write-back to the component");

            ItemStack extractedApple = items.extractItem(0, 64, false);
            helper.assertTrue(ItemStack.isSameItemSameComponents(toInsert, extractedApple), "Extracting the entire inserted item should produce the same item.");

            ItemStack extractedStick = items.extractItem(STICK_SLOT, 64, false);
            helper.assertTrue(extractedStick.getItem() == Items.STICK && extractedStick.getCount() == 64, "The extracted item from the stick slot should be a 64-count stick.");

            for (int i = 0; i < SLOTS; i++) {
                helper.assertTrue(items.getStackInSlot(i).isEmpty(), "Stack at slot " + i + " must be empty.");
            }

            helper.succeed();
        });
    }
}
