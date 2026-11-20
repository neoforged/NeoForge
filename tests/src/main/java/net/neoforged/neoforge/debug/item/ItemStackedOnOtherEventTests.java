/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;

@ForEachTest(groups = { ItemTests.GROUP + ".event", "event" })
public class ItemStackedOnOtherEventTests {
    // The first hotbar slot, as indexed by the inventory menu
    private static final int MENU_SLOT_HOTBAR_FIRST = InventoryMenu.USE_ROW_SLOT_START;
    private static final int INVENTORY_SLOT_FIRST = 0;

    private static boolean bundleContains(ItemStack bundle, Item item) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        return contents != null && contents.size() == 1 && contents.itemCopies().allMatch(stack -> stack.is(item));
    }

    private static boolean bundleIsEmpty(ItemStack bundle) {
        BundleContents contents = bundle.get(DataComponents.BUNDLE_CONTENTS);
        return contents != null && contents.isEmpty();
    }

    /** Holds the pieces every test needs after setup. */
    private record Context(ExtendedGameTestHelper helper, GameTestPlayer player, InventoryMenu menu) {
        /** Clicks the first hotbar slot with the carried item, as a player would. */
        void click(ClickAction action) {
            int button = action == ClickAction.PRIMARY ? AbstractContainerMenu.CONTAINER_CLICK_PRIMARY : AbstractContainerMenu.CONTAINER_CLICK_SECONDARY;
            menu.clicked(MENU_SLOT_HOTBAR_FIRST, button, ContainerInput.PICKUP, player);
        }

        ItemStack carried() {
            return menu.getCarried();
        }

        ItemStack inSlot() {
            return menu.getSlot(MENU_SLOT_HOTBAR_FIRST).getItem();
        }
    }

    /**
     * Common setup: a player carrying one {@code carried} in their inventory menu, with one {@code stackedOn} in the first hotbar slot.
     * The stacks are only created once the game test runs, as item components are not bound yet when the tests are collected.
     */
    private static void withInventory(DynamicTest test, GameType gameType, Item carried, Item stackedOn, Consumer<Context> body) {
        test.onGameTest(helper -> {
            GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(gameType);
            InventoryMenu menu = player.inventoryMenu;
            helper.assertTrue(player.containerMenu == menu, "Expected the inventory menu to be open");
            player.getInventory().setItem(INVENTORY_SLOT_FIRST, new ItemStack(stackedOn));
            menu.setCarried(new ItemStack(carried));

            body.accept(new Context(helper, player, menu));
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Event data, and vanilla item overrides running when not cancelled")
    static void itemStackedOnOtherEventData(DynamicTest test) {
        AtomicReference<ItemStackedOnOtherEvent> fired = new AtomicReference<>();
        // Copies taken when the event fires, as the vanilla bundle logic then modifies the stacks in place
        AtomicReference<ItemStack> carriedOnFire = new AtomicReference<>();
        AtomicReference<ItemStack> stackedOnOnFire = new AtomicReference<>();
        test.whenEnabled(listeners -> listeners.forge().addListener((ItemStackedOnOtherEvent e) -> {
            fired.set(e);
            carriedOnFire.set(e.getCarriedItem().copy());
            stackedOnOnFire.set(e.getStackedOnItem().copy());
        }));

        withInventory(test, GameType.CREATIVE, Items.BUNDLE, Items.APPLE, ctx -> {
            ctx.click(ClickAction.PRIMARY);

            ItemStackedOnOtherEvent event = fired.get();
            ctx.helper.assertNotNull(event, "Event should fire when clicking a slot with a carried item");
            ctx.helper.assertTrue(carriedOnFire.get().is(Items.BUNDLE), "Expected the bundle as carried item; carried was " + carriedOnFire.get());
            ctx.helper.assertTrue(stackedOnOnFire.get().is(Items.APPLE), "Expected the apple as stacked-on item; stacked-on was " + stackedOnOnFire.get());
            ctx.helper.assertValueEqual(event.getSlot().index, MENU_SLOT_HOTBAR_FIRST, "Expected the clicked slot; slot index is " + event.getSlot().index);
            ctx.helper.assertValueEqual(event.getClickAction(), ClickAction.PRIMARY, "Expected a primary click; action is " + event.getClickAction());
            ctx.helper.assertTrue(event.getPlayer() == ctx.player, "Expected the clicking player; player is " + event.getPlayer());
            ctx.helper.assertTrue(event.getCarriedSlotAccess().get().is(Items.BUNDLE), "Expected the carried slot access to expose the bundle; it exposes " + event.getCarriedSlotAccess().get());
            ctx.helper.assertTrue(event.getCancellationResult(), "Expected the cancellation result to be true by default, which halts the click once cancelled");

            // Not cancelled: the vanilla bundle logic ran and picked the apple up
            ctx.helper.assertTrue(ctx.inSlot().isEmpty(), "Expected the apple to be taken out of the slot; slot has " + ctx.inSlot());
            ctx.helper.assertTrue(ctx.carried().is(Items.BUNDLE), "Expected to still carry the bundle; carrying " + ctx.carried());
            ctx.helper.assertTrue(bundleContains(ctx.carried(), Items.APPLE), "Expected the apple inside the bundle; bundle is " + ctx.carried());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Cancelling with a true result halts the click entirely")
    static void itemStackedOnOtherCancelHaltsClick(DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((ItemStackedOnOtherEvent e) -> e.cancelWithResult(true)));

        withInventory(test, GameType.CREATIVE, Items.BUNDLE, Items.APPLE, ctx -> {
            ctx.click(ClickAction.PRIMARY);

            ctx.helper.assertTrue(ctx.inSlot().is(Items.APPLE), "Expected the apple to stay in the slot; slot has " + ctx.inSlot());
            ctx.helper.assertTrue(ctx.carried().is(Items.BUNDLE), "Expected to still carry the bundle; carrying " + ctx.carried());
            ctx.helper.assertTrue(bundleIsEmpty(ctx.carried()), "Expected the bundle to stay empty; bundle is " + ctx.carried());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Cancelling with a false result skips the item overrides but keeps vanilla processing")
    static void itemStackedOnOtherCancelWithVanillaProcessing(DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((ItemStackedOnOtherEvent e) -> e.cancelWithResult(false)));

        withInventory(test, GameType.CREATIVE, Items.BUNDLE, Items.APPLE, ctx -> {
            ctx.click(ClickAction.PRIMARY);

            // The bundle logic was skipped, so the two stacks were swapped instead
            ctx.helper.assertTrue(ctx.inSlot().is(Items.BUNDLE), "Expected the bundle to be put in the slot; slot has " + ctx.inSlot());
            ctx.helper.assertTrue(bundleIsEmpty(ctx.inSlot()), "Expected the bundle to stay empty; bundle is " + ctx.inSlot());
            ctx.helper.assertTrue(ctx.carried().is(Items.APPLE), "Expected to carry the apple; carrying " + ctx.carried());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "The carried slot access allows changing the carried item")
    static void itemStackedOnOtherCarriedSlotAccess(DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((ItemStackedOnOtherEvent e) -> {
            e.getCarriedSlotAccess().set(new ItemStack(Items.DIAMOND));
            e.setCanceled(true);
        }));

        withInventory(test, GameType.CREATIVE, Items.BUNDLE, Items.APPLE, ctx -> {
            ctx.click(ClickAction.PRIMARY);

            ctx.helper.assertTrue(ctx.carried().is(Items.DIAMOND), "Expected to carry the diamond set by the listener; carrying " + ctx.carried());
            ctx.helper.assertTrue(ctx.inSlot().is(Items.APPLE), "Expected the apple to stay in the slot; slot has " + ctx.inSlot());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Right-clicking a damageable item with a diamond sword damages it instead of swapping")
    static void itemStackedOnOtherDamagesStackedOnItem(DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((ItemStackedOnOtherEvent e) -> {
            if (e.getClickAction() == ClickAction.SECONDARY && e.getCarriedItem().is(Items.DIAMOND_SWORD) && e.getStackedOnItem().isDamageableItem()) {
                e.getStackedOnItem().hurtAndBreak(1, e.getPlayer(), EquipmentSlot.MAINHAND);
                e.setCanceled(true);
            }
        }));

        withInventory(test, GameType.SURVIVAL, Items.DIAMOND_SWORD, Items.IRON_PICKAXE, ctx -> {
            ctx.click(ClickAction.SECONDARY);

            ctx.helper.assertTrue(ctx.inSlot().is(Items.IRON_PICKAXE), "Expected the pickaxe to stay in the slot; slot has " + ctx.inSlot());
            ctx.helper.assertValueEqual(ctx.inSlot().getDamageValue(), 1, "Expected the pickaxe to be damaged once; damage is " + ctx.inSlot().getDamageValue());
            ctx.helper.assertTrue(ctx.carried().is(Items.DIAMOND_SWORD), "Expected to still carry the sword; carrying " + ctx.carried());

            ctx.helper.succeed();
        });
    }
}
