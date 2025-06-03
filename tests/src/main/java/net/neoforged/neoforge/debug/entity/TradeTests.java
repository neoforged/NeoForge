/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.impl.ReflectionUtils;

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
                    villager.setVillagerData(new VillagerData(helper.getHolder(VillagerType.PLAINS), helper.getHolder(VillagerProfession.WEAPONSMITH), 0));
                    try {
                        MethodHandle methodHandle = ReflectionUtils.handle(villager.getClass().getDeclaredMethod("increaseMerchantCareer"));
                        methodHandle.invoke(villager);
                        methodHandle.invoke(villager);
                        methodHandle.invoke(villager);
                        methodHandle.invoke(villager);
                        methodHandle.invoke(villager);
                        methodHandle.invoke(villager);
                    } catch (Throwable e) {
                        helper.fail("Cannot find Villager#increaseMerchantCareer method.");
                    }
                })
                .thenExecute(villager -> helper.assertTrue(villager.getOffers().size() == 9, "Weaponsmith did not get a new tier of trade"))
                .thenExecute(villager -> helper.assertTrue(villager.getOffers().get(8).getResult().is(Items.NETHERITE_SWORD), "Netherite Sword was not in trade."))
                .thenExecute(villager -> {
                    MerchantOffers originalMerchantOffers = villager.getOffers();
                    MerchantOffers newMerchantOffers;
                    try {
                        RegistryOps<Tag> registryOps = villager.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                        Tag tag = MerchantOffers.CODEC.encodeStart(registryOps, originalMerchantOffers).getOrThrow();
                        newMerchantOffers = MerchantOffers.CODEC.decode(registryOps, tag).getOrThrow().getFirst();
                    } catch (Exception e) {
                        helper.fail("Villager's modified merchant offer failed to serialize without throwing exception");
                        return;
                    }

                    if (newMerchantOffers.size() != originalMerchantOffers.size() || !newMerchantOffers.get(8).getResult().is(originalMerchantOffers.get(8).getResult().getItem())) {
                        helper.fail("Failed to serialize and deserialized modified merchant offers properly.");
                    }
                })
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
