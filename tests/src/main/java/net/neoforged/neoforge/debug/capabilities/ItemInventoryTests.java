/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.transfer.HandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.context.templates.PlayerContext;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.items.templates.ContainerContentsItemStorage;
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
    public static final int INSERT_AMOUNT = 32;
    public static final int EXTRACT_AMOUNT = 64;

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
                return new ContainerContentsItemStorage(ctx, DataComponents.CONTAINER, SLOTS);
            }, BACKPACK);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ComponentItemHandler can read and write from a data component")
    public static void testItemInventory(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            Player player = helper.makeMockPlayer();
            ItemStack stack = BACKPACK.toStack();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            IItemContext context = PlayerContext.ofHand(player, InteractionHand.MAIN_HAND);
            var items = context.getCapability(ItemHandler.ITEM);

            ItemResource storedStick = items.getResource(STICK_SLOT);
            helper.assertValueEqual(storedStick.getItem(), Items.STICK, "Default contents should contain a stick at slot " + STICK_SLOT);

            ItemResource appleResource = Items.APPLE.defaultResource;
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

            int inserted = items.insert(STICK_SLOT, appleResource, INSERT_AMOUNT, TransferAction.EXECUTE);
            helper.assertTrue(inserted == 0, "Inserting an item where it does not fit should return 0.");
            // Check identity equality to assert that the component object was not updated at all, even to an equivalent form.
            helper.assertTrue(contents == stack.get(DataComponents.CONTAINER), "Inserting an item where it does not fit should not change the component.");

            inserted = items.insert(0, appleResource, INSERT_AMOUNT, TransferAction.EXECUTE);
            helper.assertTrue(inserted == INSERT_AMOUNT, "Successfully inserting the entire item should return the amount inserted, AKA 32.");
            helper.assertTrue(HandlerUtil.resourceAndCountMatches(items, 0, appleResource, INSERT_AMOUNT), "Successfully inserting an item should be visible via the get methods");

            ItemContainerContents newContents = stack.get(DataComponents.CONTAINER);
            helper.assertTrue(ItemStack.isSameItemSameComponents(appleResource.toStack(INSERT_AMOUNT), newContents.getStackInSlot(0)), "Successfully inserting an item should trigger a write-back to the component");

            ItemResource resource = items.getResource(0);
            int extractedApple = items.extract(0, resource, INSERT_AMOUNT, TransferAction.EXECUTE);
            helper.assertTrue(extractedApple == INSERT_AMOUNT, "Extracting the entire inserted item should produce the same item.");
            helper.assertTrue(HandlerUtil.isIndexEmpty(items, 0), "Extracting the entire inserted item should leave the slot empty.");

            int extractedStick = items.extract(STICK_SLOT, Items.STICK.defaultResource, EXTRACT_AMOUNT, TransferAction.EXECUTE);
            helper.assertTrue(extractedStick == EXTRACT_AMOUNT, "The extracted item from the stick slot should be a 64-count stick.");
            helper.assertTrue(HandlerUtil.isIndexEmpty(items, STICK_SLOT), "Extracting the entire stack should leave the slot empty.");

            for (int i = 0; i < SLOTS; i++) {
                helper.assertTrue(HandlerUtil.isIndexEmpty(items, i), "Stack at slot " + i + " must be empty.");
            }

            helper.succeed();
        });
    }
}
