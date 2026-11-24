/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.crafting;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@SuppressWarnings("unused")
@ForEachTest(groups = "crafting.event")
public class GrindstoneEventTests {
    private static final int MENU_SLOT_TOP = GrindstoneMenu.INPUT_SLOT;
    private static final int MENU_SLOT_BOTTOM = GrindstoneMenu.ADDITIONAL_SLOT;
    private static final int MENU_SLOT_RESULT = GrindstoneMenu.RESULT_SLOT;
    private static final int MENU_SLOT_INV_FIRST = 30;
    private static final int MENU_SLOT_INV_SECOND = 31;
    // MENU_SLOT_INV_FIRST is the same in the menu as INVENTORY_SLOT_FIRST in player's inventory
    private static final int INVENTORY_SLOT_FIRST = 0;
    private static final int INVENTORY_SLOT_SECOND = 1;

    // Awarded as a single experience orb, so the amount can be read back from the level. It is an amount vanilla can never award
    // for the inputs of these tests: the sword with sharpness I is worth 1 XP (see vanillaXpRange), the pickaxe and the shovel 0
    private static final int CUSTOM_XP = 7;

    /** An enchanted sword, which vanilla disenchants when placed in the grindstone. */
    private static ItemStack enchantedSword(ExtendedGameTestHelper helper) {
        ItemStack s = new ItemStack(Items.IRON_SWORD);
        s.enchant(helper.getHolder(Enchantments.SHARPNESS), 1);
        return s;
    }

    /** The XP vanilla may award for some inputs, as a range of amounts. */
    private record XpRange(int min, int max) {
        boolean contains(int xp) {
            return xp >= min && xp <= max;
        }
    }

    /**
     * Computes the XP vanilla may award for disenchanting the given inputs, like the grindstone menu does: half the sum of the
     * minimum costs of their enchantments, rounded up, plus a random amount below that half. Unenchanted inputs award nothing.
     */
    private static XpRange vanillaXpRange(ItemStack... inputs) {
        int cost = 0;
        for (ItemStack input : inputs) {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(input).entrySet()) {
                if (!entry.getKey().is(EnchantmentTags.CURSE)) {
                    cost += entry.getKey().value().getMinCost(entry.getIntValue());
                }
            }
        }
        if (cost == 0) {
            return new XpRange(0, 0);
        }
        int half = (int) Math.ceil(cost / 2.0);
        return new XpRange(half, half * 2 - 1);
    }

    private static void moveItemsToInputs(GrindstoneMenu menu, Player player) {
        menu.clicked(MENU_SLOT_INV_FIRST, AbstractContainerMenu.CONTAINER_CLICK_PRIMARY, ContainerInput.QUICK_MOVE, player);
        menu.clicked(MENU_SLOT_INV_SECOND, AbstractContainerMenu.CONTAINER_CLICK_PRIMARY, ContainerInput.QUICK_MOVE, player);
    }

    /** Picks the result up, as a player would, and returns the stack now carried. */
    private static ItemStack takeResult(GrindstoneMenu menu, Player player) {
        menu.clicked(MENU_SLOT_RESULT, AbstractContainerMenu.CONTAINER_CLICK_PRIMARY, ContainerInput.PICKUP, player);
        return menu.getCarried();
    }

    /** The total value of the experience orbs around the grindstone. */
    private static int experienceAroundGrindstone(ExtendedGameTestHelper helper) {
        int total = 0;
        for (ExperienceOrb orb : helper.getEntities(EntityTypes.EXPERIENCE_ORB, BlockPos.ZERO, 2)) {
            total += orb.getValue();
        }
        return total;
    }

    /** Overrides the output and XP of a pickaxe + shovel combination; vanilla would produce nothing for it. */
    private static void pickaxeAndShovelToDiamond(GrindstoneEvent.OnPlaceItem event) {
        if (event.getTopItem().is(Items.IRON_PICKAXE) && event.getBottomItem().is(Items.IRON_SHOVEL)) {
            event.setOutput(new ItemStack(Items.DIAMOND));
            event.setXp(CUSTOM_XP);
        }
    }

    /** Holds the pieces every test needs after setup. */
    private record Context(ExtendedGameTestHelper helper, Player player, GrindstoneMenu menu) {}

    /**
     * Common grindstone, player and menu setup.
     */
    private static void withGrindstone(DynamicTest test, Consumer<Context> body) {
        test.onGameTest(helper -> {
            helper.setBlock(BlockPos.ZERO, Blocks.GRINDSTONE);
            Player player = helper.makeTickingMockServerPlayerInLevel(GameType.CREATIVE);
            BlockPos pos = helper.absolutePos(BlockPos.ZERO);
            var provider = helper.getBlockState(BlockPos.ZERO).getMenuProvider(helper.getLevel(), pos);
            helper.assertNotNull(provider, "MenuProvider missing for grindstone");
            player.openMenu(provider);

            helper.assertTrue(
                    player.containerMenu instanceof GrindstoneMenu,
                    "Expected GrindstoneMenu but got " + player.containerMenu.getClass().getName());
            GrindstoneMenu menu = (GrindstoneMenu) player.containerMenu;

            body.accept(new Context(helper, player, menu));
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Vanilla disenchanting, with both events firing")
    static void grindstoneVanillaDisenchant(DynamicTest test) {
        AtomicReference<GrindstoneEvent.OnPlaceItem> placeEvent = new AtomicReference<>();
        AtomicReference<GrindstoneEvent.OnTakeItem> takeEvent = new AtomicReference<>();
        test.whenEnabled(listeners -> {
            listeners.forge().addListener((GrindstoneEvent.OnPlaceItem e) -> placeEvent.set(e));
            listeners.forge().addListener((GrindstoneEvent.OnTakeItem e) -> takeEvent.set(e));
        });

        withGrindstone(test, ctx -> {
            ctx.player.getInventory().setItem(INVENTORY_SLOT_FIRST, enchantedSword(ctx.helper));

            moveItemsToInputs(ctx.menu, ctx.player);
            GrindstoneEvent.OnPlaceItem place = placeEvent.get();
            ctx.helper.assertNotNull(place, "OnPlaceItem should fire when placing an item in the top slot");
            ctx.helper.assertTrue(place.getTopItem().is(Items.IRON_SWORD), "Expected the sword as top item; top is " + place.getTopItem());
            ctx.helper.assertTrue(place.getBottomItem().isEmpty(), "Expected an empty bottom item; bottom is " + place.getBottomItem());
            ctx.helper.assertTrue(place.getOutput().isEmpty(), "Expected an empty output before any listener sets it; output is " + place.getOutput());
            ctx.helper.assertValueEqual(place.getXp(), -1, "Expected the default XP of -1; XP is " + place.getXp());

            ItemStack out = ctx.menu.getSlot(MENU_SLOT_RESULT).getItem().copy();
            ctx.helper.assertTrue(
                    out.is(Items.IRON_SWORD) && !out.isEnchanted(),
                    "Expected a disenchanted Iron Sword, got " + out);

            ItemStack taken = takeResult(ctx.menu, ctx.player);
            ctx.helper.assertTrue(ItemStack.isSameItemSameComponents(taken, out), "Expected to carry the disenchanted sword; carrying " + taken);

            GrindstoneEvent.OnTakeItem take = takeEvent.get();
            ctx.helper.assertNotNull(take, "OnTakeItem should fire when taking the result");
            ctx.helper.assertTrue(take.getPlayer() == ctx.player, "Expected the player using the grindstone; player is " + take.getPlayer());
            ctx.helper.assertTrue(take.getTopItem().is(Items.IRON_SWORD), "Expected the sword as top item; top is " + take.getTopItem());
            XpRange vanillaXp = vanillaXpRange(enchantedSword(ctx.helper));
            ctx.helper.assertTrue(vanillaXp.contains(take.getXp()), "Expected the vanilla XP amount, within " + vanillaXp + "; XP is " + take.getXp());
            int awardedXp = experienceAroundGrindstone(ctx.helper);
            ctx.helper.assertTrue(vanillaXp.contains(awardedXp), "Expected the vanilla XP amount to be awarded, within " + vanillaXp + "; awarded " + awardedXp);
            ctx.helper.assertTrue(ctx.menu.getSlot(MENU_SLOT_TOP).getItem().isEmpty(), "Expected the top input to be consumed; top is " + ctx.menu.getSlot(MENU_SLOT_TOP).getItem());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Custom output & XP override on place, XP awarded on take")
    static void grindstoneCustomOutputAndXp(DynamicTest test) {
        AtomicReference<GrindstoneEvent.OnTakeItem> takeEvent = new AtomicReference<>();
        test.whenEnabled(listeners -> {
            listeners.forge().addListener(GrindstoneEventTests::pickaxeAndShovelToDiamond);
            listeners.forge().addListener((GrindstoneEvent.OnTakeItem e) -> takeEvent.set(e));
        });

        withGrindstone(test, ctx -> {
            ctx.player.getInventory().setItem(INVENTORY_SLOT_FIRST, new ItemStack(Items.IRON_PICKAXE));
            ctx.player.getInventory().setItem(INVENTORY_SLOT_SECOND, new ItemStack(Items.IRON_SHOVEL));

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack out = ctx.menu.getSlot(MENU_SLOT_RESULT).getItem();
            ctx.helper.assertTrue(out.is(Items.DIAMOND), "Expected the custom diamond output; output is " + out);

            ItemStack taken = takeResult(ctx.menu, ctx.player);
            ctx.helper.assertTrue(taken.is(Items.DIAMOND), "Expected to carry the diamond; carrying " + taken);

            GrindstoneEvent.OnTakeItem take = takeEvent.get();
            ctx.helper.assertNotNull(take, "OnTakeItem should fire when taking the result");
            ctx.helper.assertValueEqual(take.getXp(), CUSTOM_XP, "Expected the XP set on place to be passed on take; XP is " + take.getXp());
            ctx.helper.assertTrue(
                    take.getContainerAccess().evaluate((level, pos) -> pos.equals(ctx.helper.absolutePos(BlockPos.ZERO))).orElse(false),
                    "Expected the container access to point at the grindstone");
            ctx.helper.assertValueEqual(experienceAroundGrindstone(ctx.helper), CUSTOM_XP, "Expected the custom XP amount to be awarded");
            ctx.helper.assertTrue(ctx.menu.getSlot(MENU_SLOT_TOP).getItem().isEmpty(), "Expected the top input to be consumed; top is " + ctx.menu.getSlot(MENU_SLOT_TOP).getItem());
            ctx.helper.assertTrue(ctx.menu.getSlot(MENU_SLOT_BOTTOM).getItem().isEmpty(), "Expected the bottom input to be consumed; bottom is " + ctx.menu.getSlot(MENU_SLOT_BOTTOM).getItem());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Cancelled place event clears output")
    static void grindstoneCancelClearsOutput(DynamicTest test) {
        test.whenEnabled(listeners -> listeners.forge().addListener((GrindstoneEvent.OnPlaceItem e) -> e.setCanceled(true)));

        withGrindstone(test, ctx -> {
            ctx.player.getInventory().setItem(INVENTORY_SLOT_FIRST, enchantedSword(ctx.helper));

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack out = ctx.menu.getSlot(MENU_SLOT_RESULT).getItem();
            ctx.helper.assertTrue(out.isEmpty(), "Expected no output when cancelled; output is " + out);

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Empty output runs vanilla behaviour and ignores the XP override")
    static void grindstoneEmptyOutputRunsVanilla(DynamicTest test) {
        AtomicReference<GrindstoneEvent.OnTakeItem> takeEvent = new AtomicReference<>();
        test.whenEnabled(listeners -> {
            listeners.forge().addListener((GrindstoneEvent.OnPlaceItem e) -> e.setXp(CUSTOM_XP));
            listeners.forge().addListener((GrindstoneEvent.OnTakeItem e) -> takeEvent.set(e));
        });

        withGrindstone(test, ctx -> {
            ctx.player.getInventory().setItem(INVENTORY_SLOT_FIRST, enchantedSword(ctx.helper));

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack out = ctx.menu.getSlot(MENU_SLOT_RESULT).getItem();
            ctx.helper.assertTrue(
                    out.is(Items.IRON_SWORD) && !out.isEnchanted(),
                    "Expected the vanilla disenchanted Iron Sword, got " + out);

            takeResult(ctx.menu, ctx.player);
            GrindstoneEvent.OnTakeItem take = takeEvent.get();
            ctx.helper.assertNotNull(take, "OnTakeItem should fire when taking the result");
            // CUSTOM_XP lies outside the vanilla range for this input, so this also shows that the override on place was ignored
            XpRange vanillaXp = vanillaXpRange(enchantedSword(ctx.helper));
            ctx.helper.assertTrue(vanillaXp.contains(take.getXp()), "Expected the vanilla XP amount, within " + vanillaXp + ", when the output is left empty; XP is " + take.getXp());

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Take event overrides the remaining inputs and the XP")
    static void grindstoneTakeOverridesInputsAndXp(DynamicTest test) {
        test.whenEnabled(listeners -> {
            listeners.forge().addListener(GrindstoneEventTests::pickaxeAndShovelToDiamond);
            listeners.forge().addListener((GrindstoneEvent.OnTakeItem e) -> {
                // Keep the top input, consume the bottom one and award no XP
                e.setNewTopItem(e.getTopItem().copy());
                e.setNewBottomItem(ItemStack.EMPTY);
                e.setXp(0);
            });
        });

        withGrindstone(test, ctx -> {
            ctx.player.getInventory().setItem(INVENTORY_SLOT_FIRST, new ItemStack(Items.IRON_PICKAXE));
            ctx.player.getInventory().setItem(INVENTORY_SLOT_SECOND, new ItemStack(Items.IRON_SHOVEL));

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack taken = takeResult(ctx.menu, ctx.player);
            ctx.helper.assertTrue(taken.is(Items.DIAMOND), "Expected to carry the diamond; carrying " + taken);

            ItemStack top = ctx.menu.getSlot(MENU_SLOT_TOP).getItem();
            ItemStack bottom = ctx.menu.getSlot(MENU_SLOT_BOTTOM).getItem();
            ctx.helper.assertTrue(top.is(Items.IRON_PICKAXE), "Expected the top input to be kept; top is " + top);
            ctx.helper.assertTrue(bottom.isEmpty(), "Expected the bottom input to be consumed; bottom is " + bottom);
            ctx.helper.assertValueEqual(experienceAroundGrindstone(ctx.helper), 0, "Expected no XP to be awarded");
            // The output is computed again from the new inputs, for which vanilla produces nothing
            ItemStack out = ctx.menu.getSlot(MENU_SLOT_RESULT).getItem();
            ctx.helper.assertTrue(out.isEmpty(), "Expected no output for the remaining input; output is " + out);

            ctx.helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Cancelled take event keeps the inputs and awards no XP")
    static void grindstoneCancelTakeKeepsInputs(DynamicTest test) {
        test.whenEnabled(listeners -> {
            listeners.forge().addListener(GrindstoneEventTests::pickaxeAndShovelToDiamond);
            listeners.forge().addListener((GrindstoneEvent.OnTakeItem e) -> e.setCanceled(true));
        });

        withGrindstone(test, ctx -> {
            ctx.player.getInventory().setItem(INVENTORY_SLOT_FIRST, new ItemStack(Items.IRON_PICKAXE));
            ctx.player.getInventory().setItem(INVENTORY_SLOT_SECOND, new ItemStack(Items.IRON_SHOVEL));

            moveItemsToInputs(ctx.menu, ctx.player);
            ItemStack taken = takeResult(ctx.menu, ctx.player);
            ctx.helper.assertTrue(taken.is(Items.DIAMOND), "Expected to carry the diamond; carrying " + taken);

            ItemStack top = ctx.menu.getSlot(MENU_SLOT_TOP).getItem();
            ItemStack bottom = ctx.menu.getSlot(MENU_SLOT_BOTTOM).getItem();
            ctx.helper.assertTrue(top.is(Items.IRON_PICKAXE), "Expected the top input to be kept when cancelled; top is " + top);
            ctx.helper.assertTrue(bottom.is(Items.IRON_SHOVEL), "Expected the bottom input to be kept when cancelled; bottom is " + bottom);
            ctx.helper.assertValueEqual(experienceAroundGrindstone(ctx.helper), 0, "Expected no XP to be awarded when cancelled");

            ctx.helper.succeed();
        });
    }
}
