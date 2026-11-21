/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;

@ForEachTest(groups = { PlayerTests.GROUP + ".event", "event" })
public class TradeWithVillagerEventTests {
    private static final int MENU_SLOT_PAYMENT = 0;
    private static final int MENU_SLOT_RESULT = 2;
    private static final int INVENTORY_SLOT_FIRST = 0;
    private static final int EMERALD_COST = 3;
    private static final BlockPos TRADER_POS = new BlockPos(1, 2, 1);

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the TradeWithVillagerEvent fires when trading with a villager")
    static void tradeWithVillager(DynamicTest test) {
        tradeTest(test, EntityTypes.VILLAGER);
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the TradeWithVillagerEvent fires when trading with a wandering trader")
    static void tradeWithWanderingTrader(DynamicTest test) {
        tradeTest(test, EntityTypes.WANDERING_TRADER);
    }

    /**
     * Spawns a trader with a single emeralds-for-bread offer, then completes that trade as a player would:
     * interact with the trader, move the emeralds into the payment slot and pick the result up.
     */
    private static <T extends AbstractVillager> void tradeTest(DynamicTest test, EntityType<T> type) {
        AtomicReference<TradeWithVillagerEvent> fired = new AtomicReference<>();
        AtomicInteger fireCount = new AtomicInteger();
        test.whenEnabled(listeners -> listeners.forge().addListener((TradeWithVillagerEvent e) -> {
            fired.set(e);
            fireCount.incrementAndGet();
        }));

        test.onGameTest(helper -> {
            T trader = helper.spawnWithNoFreeWill(type, TRADER_POS);
            MerchantOffer offer = new MerchantOffer(new ItemCost(Items.EMERALD, EMERALD_COST), new ItemStack(Items.BREAD, 2), 12, 1, 0.05F);
            MerchantOffers offers = trader.getOffers();
            offers.clear();
            offers.add(offer);

            GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL).moveToCentre();
            player.getInventory().setItem(INVENTORY_SLOT_FIRST, new ItemStack(Items.EMERALD, EMERALD_COST));

            player.interactOn(trader, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertTrue(
                    player.containerMenu instanceof MerchantMenu,
                    "Expected MerchantMenu but got " + player.containerMenu.getClass().getName());
            MerchantMenu menu = (MerchantMenu) player.containerMenu;

            menu.tryMoveItems(0);
            ItemStack result = menu.getSlot(MENU_SLOT_RESULT).getItem();
            helper.assertTrue(ItemStack.isSameItemSameComponents(result, offer.getResult()), "Expected the offer result in the result slot; result is " + result);
            helper.assertTrue(fired.get() == null, "Event should not fire before the result is taken");

            menu.clicked(MENU_SLOT_RESULT, AbstractContainerMenu.CONTAINER_CLICK_PRIMARY, ContainerInput.PICKUP, player);

            TradeWithVillagerEvent event = fired.get();
            helper.assertNotNull(event, "Event should fire when the result of a trade is taken");
            helper.assertValueEqual(fireCount.get(), 1, "Expected the event to fire once per trade; it fired " + fireCount.get() + " times");
            helper.assertTrue(event.getEntity() == player, "Expected the trading player; player is " + event.getEntity());
            helper.assertTrue(event.getAbstractVillager() == trader, "Expected the trader; villager is " + event.getAbstractVillager());
            helper.assertTrue(event.getMerchantOffer() == offer, "Expected the selected offer; offer is " + event.getMerchantOffer());

            ItemStack carried = menu.getCarried();
            helper.assertTrue(
                    ItemStack.isSameItemSameComponents(carried, offer.getResult()) && carried.getCount() == offer.getResult().getCount(),
                    "Expected to carry the offer result; carrying " + carried);
            helper.assertTrue(menu.getSlot(MENU_SLOT_PAYMENT).getItem().isEmpty(), "Expected the emeralds to be consumed; payment is " + menu.getSlot(MENU_SLOT_PAYMENT).getItem());
            helper.assertValueEqual(offer.getUses(), 1, "Expected the offer to be used once; uses is " + offer.getUses());

            player.closeContainer();
            helper.succeed();
        });
    }
}
