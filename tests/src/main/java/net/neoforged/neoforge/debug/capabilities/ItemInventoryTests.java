/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
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
        BACKPACK = ITEMS.registerItem("test_backpack", Item::new);
    }

    @OnInit
    static void init(final TestFramework framework) {
        ITEMS.register(framework.modEventBus());
        framework.modEventBus().<RegisterCapabilitiesEvent>addListener(e -> {
            e.registerItem(Capabilities.Item.ITEM, (stack, itemAccess) -> {
                return new ItemAccessItemHandler(itemAccess, DataComponents.CONTAINER, SLOTS);
            }, BACKPACK);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ComponentItemHandler can read and write from a data component")
    public static void testItemContainer(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            ItemStack container = BACKPACK.toStack();
            NonNullList<ItemStack> defaultContents = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
            defaultContents.set(STICK_SLOT, Items.STICK.getDefaultInstance().copyWithCount(64));
            container.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(defaultContents));

            ItemAccess itemAccess = ItemAccess.forStack(container);
            // Note: this uses the legacy wrappers, testing the wrappers and that the new ItemAccessItemHandler matches the old ComponentItemHandler.
            IItemHandler items = IItemHandler.of(itemAccess.getCapability(Capabilities.Item.ITEM));

            ItemStack storedStick = items.getStackInSlot(STICK_SLOT);
            helper.assertValueEqual(storedStick.getItem(), Items.STICK, "Default contents should contain a stick at slot " + STICK_SLOT);

            ItemStack toInsert = Items.APPLE.getDefaultInstance().copyWithCount(32);
            ItemContainerContents contents = container.get(DataComponents.CONTAINER);

            ItemStack remainder = items.insertItem(STICK_SLOT, toInsert, false);
            helper.assertTrue(ItemStack.matches(toInsert, remainder), "Inserting an item where it does not fit should return the original item.");
            // Check identity equality to assert that the component object was not updated at all, even to an equivalent form.
            helper.assertTrue(contents == container.get(DataComponents.CONTAINER), "Inserting an item where it does not fit should not change the component.");

            remainder = items.insertItem(0, toInsert, false);
            helper.assertTrue(remainder.isEmpty(), "Successfully inserting the entire item should return an empty stack.");
            helper.assertTrue(ItemStack.matches(toInsert, items.getStackInSlot(0)), "Successfully inserting an item should be visible via getStackInSlot");

            ItemContainerContents newContents = container.get(DataComponents.CONTAINER);
            helper.assertTrue(ItemStack.matches(toInsert, newContents.getStackInSlot(0)), "Successfully inserting an item should trigger a write-back to the component");

            ItemStack extractedApple = items.extractItem(0, 64, false);
            helper.assertTrue(ItemStack.matches(toInsert, extractedApple), "Extracting the entire inserted item should produce the same item.");

            ItemStack extractedStick = items.extractItem(STICK_SLOT, 64, false);
            helper.assertTrue(extractedStick.getItem() == Items.STICK && extractedStick.getCount() == 64, "The extracted item from the stick slot should be a 64-count stick.");

            for (int i = 0; i < SLOTS; i++) {
                helper.assertTrue(items.getStackInSlot(i).isEmpty(), "Stack at slot " + i + " must be empty.");
            }

            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that BundleItemHandler can read and write from a data component")
    public static void testItemBundle(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            ItemStack bundle = Items.BUNDLE.getDefaultInstance();
            BundleContents.Mutable mutable = new BundleContents.Mutable(bundle.get(DataComponents.BUNDLE_CONTENTS));
            mutable.tryInsert(Items.STICK.getDefaultInstance().copyWithCount(16));
            mutable.tryInsert(Items.APPLE.getDefaultInstance().copyWithCount(16));
            bundle.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());

            ItemAccess itemAccess = ItemAccess.forStack(bundle);
            IItemHandler items = IItemHandler.of(itemAccess.getCapability(Capabilities.Item.ITEM));

            helper.assertValueEqual(items.getSlots(), 3, "Bundle with 2 stacks should report 3 slots (2 items + 1 empty).");

            boolean foundStick = false;
            boolean foundApple = false;
            for (int i = 0; i < 2; i++) {
                ItemStack s = items.getStackInSlot(i);
                if (s.getItem() == Items.STICK && s.getCount() == 16) foundStick = true;
                if (s.getItem() == Items.APPLE && s.getCount() == 16) foundApple = true;
            }
            helper.assertTrue(foundStick, "Bundle should contain 16 sticks.");
            helper.assertTrue(foundApple, "Bundle should contain 16 apples.");

            ItemStack moreApples = new ItemStack(Items.APPLE, 32);
            ItemStack remainder = items.insertItem(2, moreApples, false);
            helper.assertTrue(remainder.isEmpty(), "Should be able to insert 32 more apples.");

            helper.assertValueEqual(items.getSlots(), 2, "After merging to full, slot count should be 2.");

            int appleCount = 0;
            for (int i = 0; i < 2; i++) {
                if (items.getStackInSlot(i).getItem() == Items.APPLE) appleCount += items.getStackInSlot(i).getCount();
            }
            helper.assertValueEqual(appleCount, 48, "Total apples should be 16 + 32 = 48.");

            ItemStack excessApple = new ItemStack(Items.APPLE, 1);
            remainder = items.insertItem(2, excessApple, false);
            helper.assertValueEqual(remainder.getCount(), 1, "Should not accept apple when bundle is full.");

            bundle.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            helper.assertValueEqual(items.getSlots(), 1, "Empty bundle should have 1 slot.");

            ItemStack snowballs = new ItemStack(Items.SNOWBALL, 16);
            remainder = items.insertItem(0, snowballs, false);
            helper.assertTrue(remainder.isEmpty(), "Should accept 16 snowballs.");

            remainder = items.insertItem(1, new ItemStack(Items.STICK), false);
            helper.assertValueEqual(remainder.getCount(), 1, "Full bundle should not accept sticks.");

            bundle.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);

            ItemStack shulker = new ItemStack(Items.SHULKER_BOX);
            remainder = items.insertItem(0, shulker, false);
            helper.assertValueEqual(remainder.getCount(), 1, "Bundle should reject Shulker Box.");

            items.insertItem(0, new ItemStack(Items.DIRT, 32), false);

            ItemStack extracted = items.extractItem(0, 16, false);
            helper.assertValueEqual(extracted.getCount(), 16, "Should extract 16 dirt.");
            helper.assertValueEqual(extracted.getItem(), Items.DIRT, "Should be dirt.");

            helper.assertValueEqual(items.getStackInSlot(0).getCount(), 16, "Bundle should have 16 dirt left.");

            extracted = items.extractItem(0, 64, false);
            helper.assertValueEqual(extracted.getCount(), 16, "Should extract remaining 16 dirt.");

            helper.assertValueEqual(items.getSlots(), 1, "Empty bundle should have 1 slot.");
            helper.assertTrue(items.getStackInSlot(0).isEmpty(), "Slot 0 should be empty.");

            helper.succeed();
        });
    }
}
