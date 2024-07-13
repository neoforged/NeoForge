/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.mixins.VillagerAccessor;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = TradeTests.GROUP)
public class TradeTests {
    public static final String GROUP = "level.entity.trades";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Verify we can add a trade to a villager")
    static void villagerTradeAdd(final DynamicTest test) {
        NeoForge.EVENT_BUS.addListener(TradeTests::villagerTradeAddEvent);

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawn(EntityType.VILLAGER, new BlockPos(1, 1, 1)))
                .thenExecute(villager -> {
                    // Set villager to level 6 trades
                    villager.setVillagerData(new VillagerData(VillagerType.PLAINS, VillagerProfession.WEAPONSMITH, 0));
                    ((VillagerAccessor) villager).neoforge$callIncreaseMerchantCareer();
                    ((VillagerAccessor) villager).neoforge$callIncreaseMerchantCareer();
                    ((VillagerAccessor) villager).neoforge$callIncreaseMerchantCareer();
                    ((VillagerAccessor) villager).neoforge$callIncreaseMerchantCareer();
                    ((VillagerAccessor) villager).neoforge$callIncreaseMerchantCareer();
                    ((VillagerAccessor) villager).neoforge$callIncreaseMerchantCareer();
                })
                .thenExecute(villager -> helper.assertTrue(villager.getOffers().size() == 9, "Weaponsmith did not get a new tier of trade"))
                .thenExecute(villager -> helper.assertTrue(villager.getOffers().get(8).getResult().is(Items.NETHERITE_SWORD), "Netherite Sword was not in trade."))
                .thenSucceed());
    }

    public static void villagerTradeAddEvent(final VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.WEAPONSMITH) {
            ArrayList<VillagerTrades.ItemListing> itemListings = new ArrayList<>();
            itemListings.add(new BasicItemListing(new ItemStack(Items.EMERALD, 5), new ItemStack(Items.NETHERITE_SWORD), 10, 20, 2));
            event.getTrades().put(6, itemListings);
        }
    }
}
