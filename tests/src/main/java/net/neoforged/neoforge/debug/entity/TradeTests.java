/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

import java.util.ArrayList;

@ForEachTest(groups = TradeTests.GROUP)
public class TradeTests {
    public static final String GROUP = "level.entity.trades";
    private static final String MOD_ID = "villager_trade_test";


    @Mod(value = MOD_ID)
    public static class VillagerTradeTestMod {
        public VillagerTradeTestMod() {
            IEventBus eventBus = NeoForge.EVENT_BUS;
            eventBus.addListener(this::villagerTradeAddEvent);
        }

        public void villagerTradeAddEvent(final VillagerTradesEvent event) {
            if (event.getType() == VillagerProfession.WEAPONSMITH) {
                ArrayList<VillagerTrades.ItemListing> itemListings = new ArrayList<>();
                itemListings.add(new BasicItemListing(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.NETHERITE_SWORD), 10, 20, 2));
                event.getTrades().put(6, itemListings);
            }
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Verify we can add a trade to a villager")
    static void villagerTradeAdd(final DynamicTest test) {
        test.onGameTest(helper ->
            helper.startSequence(() -> helper.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 1)))
                .thenExecute(villager -> villager.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.WEAPONSMITH, 6)))
                .thenExecute(villager -> helper.assertTrue(villager.getOffers().size() == 1, "Weaponsmith did not get a new tier of trade"))
                .thenExecute(villager -> helper.assertTrue(villager.getOffers().getFirst().getResult().is(Items.NETHERITE_SWORD), "Netherite Sword was not in trade."))
                .thenSucceed());
    }
}
