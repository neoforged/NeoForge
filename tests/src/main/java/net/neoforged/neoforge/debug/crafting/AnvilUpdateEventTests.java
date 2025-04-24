/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@SuppressWarnings("unused")
@ForEachTest(groups = "crafting.event")
public class AnvilUpdateEventTests {
    private static final int SLOT_LEFT = 0;
    private static final int SLOT_RIGHT = 1;
    private static final int SLOT_RESULT = 2;
    private static final int SLOT_INV_FIRST = 30;
    private static final int SLOT_INV_SECOND = 31;

    private static final ItemStack MOCK_OUTPUT = new ItemStack(Items.COBBLESTONE);

    private static ItemStack sampleStack() {
        ItemStack s = new ItemStack(Items.GOLDEN_HOE, 1);
        s.setDamageValue(4);
        return s;
    }

    private static void moveItemsToInputs(AnvilMenu menu, Player player) {
        menu.clicked(SLOT_INV_FIRST, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
        menu.clicked(SLOT_INV_SECOND, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
    }

    private static void clearInputs(AnvilMenu menu, Player player) {
        menu.clicked(SLOT_LEFT, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
        menu.clicked(SLOT_RIGHT, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, player);
    }

    /** Holds the pieces every test needs after setup. */
    private record Context(ExtendedGameTestHelper helper, Player player, AnvilMenu menu) {}

    /**
     * Common anvil, player and menu setup.
     */
    private static void withAnvil(DynamicTest test, Consumer<Context> body) {
        test.onGameTest(helper -> {
            helper.setBlock(BlockPos.ZERO, Blocks.ANVIL);
            Player player = helper.makeTickingMockServerPlayerInLevel(GameType.CREATIVE);
            var provider = helper.getBlockState(BlockPos.ZERO).getMenuProvider(helper.getLevel(), BlockPos.ZERO);
            helper.assertNotNull(provider, "MenuProvider missing for anvil");
            player.openMenu(provider);

            helper.assertTrue(
                    player.containerMenu instanceof AnvilMenu,
                    "Expected AnvilMenu but got " + player.containerMenu.getClass().getName());
            AnvilMenu menu = (AnvilMenu) player.containerMenu;

            body.accept(new Context(helper, player, menu));
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Vanilla combine")
    static void vanillaCombineTest(DynamicTest test) {
        withAnvil(test, ctx -> {
            ctx.player.getInventory().setItem(SLOT_LEFT, sampleStack().copy());
            ctx.player.getInventory().setItem(SLOT_RIGHT, sampleStack().copy());

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack out = ctx.menu.getSlot(SLOT_RESULT).getItem();
            ctx.helper.assertTrue(
                    out.is(Items.GOLDEN_HOE) && out.getDamageValue() == 0,
                    "Expected fully-repaired Golden Hoe, got " + out);

            clearInputs(ctx.menu, ctx.player);
            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Custom output & cost & material cost override")
    static void customOutputAndCostTest(DynamicTest test) {
        final int CUSTOM_COST = 7;
        final int CUSTOM_MATERIAL_COST = 3;

        test.whenEnabled(listeners -> listeners.forge().addListener((AnvilUpdateEvent e) -> {
            e.setOutput(MOCK_OUTPUT.copy());
            e.setCost(CUSTOM_COST);
            e.setMaterialCost(CUSTOM_MATERIAL_COST);
        }));

        withAnvil(test, ctx -> {
            ctx.player.getInventory().setItem(SLOT_LEFT, sampleStack().copy());
            ctx.player.getInventory().setItem(SLOT_RIGHT, sampleStack().copy());

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack out = ctx.menu.getSlot(SLOT_RESULT).getItem();
            ctx.helper.assertTrue(
                    ItemStack.isSameItemSameComponents(out, MOCK_OUTPUT),
                    "Expected custom cobblestone output; output is " + out);
            ctx.helper.assertValueEqual(
                    CUSTOM_COST, ctx.menu.getCost(),
                    "Cost override not applied; cost is " + ctx.menu.getCost());
            ctx.helper.assertValueEqual(
                    CUSTOM_MATERIAL_COST, ctx.menu.repairItemCountCost,
                    "Material-cost override not applied; material-cost is " + ctx.menu.repairItemCountCost);

            clearInputs(ctx.menu, ctx.player);
            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Cancelled event clears output")
    static void cancelClearsOutputTest(DynamicTest test) {
        AtomicBoolean cancel = new AtomicBoolean(false);
        test.whenEnabled(listeners -> listeners.forge().addListener(
                (AnvilUpdateEvent e) -> e.setCanceled(cancel.get())));

        final String CUSTOM_NAME = "Hoe - 3719436245";
        withAnvil(test, ctx -> {
            ctx.player.getInventory().setItem(SLOT_LEFT, sampleStack().copy());
            ctx.player.getInventory().setItem(SLOT_RIGHT, sampleStack().copy());

            ctx.menu.clicked(SLOT_INV_FIRST, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, ctx.player);
            ctx.menu.setItemName(CUSTOM_NAME);
            cancel.set(true);
            ctx.menu.clicked(SLOT_INV_SECOND, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, ctx.player);
            ItemStack out = ctx.menu.getSlot(SLOT_RESULT).getItem();

            ctx.helper.assertTrue(out.isEmpty(), "Expected no output when cancelled; output is " + out);

            clearInputs(ctx.menu, ctx.player);
            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Renaming item")
    static void renameTest(DynamicTest test) {
        final String CUSTOM_NAME = "Hoe - 5784351896";

        withAnvil(test, ctx -> {
            ctx.player.getInventory().setItem(SLOT_LEFT, sampleStack().copy());

            moveItemsToInputs(ctx.menu, ctx.player);
            ctx.menu.setItemName(CUSTOM_NAME);

            ItemStack out = ctx.menu.getSlot(SLOT_RESULT).getItem();
            ctx.helper.assertValueEqual(
                    CUSTOM_NAME, out.getHoverName().getString(),
                    "Unexpected output name; output is " + out.getHoverName().getString());

            clearInputs(ctx.menu, ctx.player);
            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Removing left input resets result")
    static void leftRemovalResetsOutputTest(DynamicTest test) {
        withAnvil(test, ctx -> {
            ctx.player.getInventory().setItem(SLOT_LEFT, sampleStack().copy());
            ctx.player.getInventory().setItem(SLOT_RIGHT, sampleStack().copy());

            moveItemsToInputs(ctx.menu, ctx.player);
            ctx.menu.clicked(SLOT_LEFT, InputConstants.MOUSE_BUTTON_LEFT, ClickType.QUICK_MOVE, ctx.player);

            ItemStack out = ctx.menu.getSlot(SLOT_RESULT).getItem();
            ctx.helper.assertTrue(out.isEmpty(), "Expected result to reset when left slot cleared; the result is " + out);

            ctx.helper.succeed();
        });
    }
}
