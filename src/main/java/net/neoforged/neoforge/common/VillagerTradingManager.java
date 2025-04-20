/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.event.village.WandererTradesEvent;
import org.apache.commons.lang3.tuple.Pair;

public class VillagerTradingManager {
    private static final Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> VANILLA_TRADES = new HashMap<>();
    private static final List<Pair<ItemListing[], Integer>> WANDERER_TRADES = new ArrayList<>();

    static {
        VillagerTrades.TRADES.entrySet().forEach(e -> {
            Int2ObjectMap<ItemListing[]> copy = new Int2ObjectOpenHashMap<>();
            e.getValue().int2ObjectEntrySet().forEach(ent -> copy.put(ent.getIntKey(), Arrays.copyOf(ent.getValue(), ent.getValue().length)));
            VANILLA_TRADES.put(e.getKey(), copy);
        });
        VillagerTrades.WANDERING_TRADER_TRADES.forEach(e -> WANDERER_TRADES.add(Pair.of(Arrays.copyOf(e.getLeft(), e.getLeft().length), e.getRight())));
    }

    static void loadTrades(TagsUpdatedEvent e) {
        if (e.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            postWandererEvent(e.getLookupProvider());
            postVillagerEvents(e.getLookupProvider());
        }
    }

    /**
     * Posts the WandererTradesEvent.
     */
    private static void postWandererEvent(HolderLookup.Provider registries) {
        List<ItemListing> buying = NonNullList.create();
        List<ItemListing> rare = NonNullList.create();
        List<ItemListing> generic = NonNullList.create();
        Arrays.stream(WANDERER_TRADES.get(0).getLeft()).forEach(buying::add);
        Arrays.stream(WANDERER_TRADES.get(1).getLeft()).forEach(rare::add);
        Arrays.stream(WANDERER_TRADES.get(2).getLeft()).forEach(generic::add);
        int buyingAmount = WANDERER_TRADES.get(0).getRight();
        int rareAmount = WANDERER_TRADES.get(1).getRight();
        int genericAmount = WANDERER_TRADES.get(2).getRight();
        List<Pair<List<ItemListing>, Integer>> additional = new ArrayList<>();

        WandererTradesEvent event = new WandererTradesEvent(buying, buyingAmount, rare, rareAmount, generic, genericAmount, additional, registries);
        NeoForge.EVENT_BUS.post(event);

        VillagerTrades.WANDERING_TRADER_TRADES = ImmutableList.<Pair<ItemListing[], Integer>>builder()
                .add(Pair.of(buying.toArray(ItemListing[]::new), event.getBuyingAmount()))
                .add(Pair.of(rare.toArray(ItemListing[]::new), event.getRareAmount()))
                .add(Pair.of(generic.toArray(ItemListing[]::new), event.getGenericAmount()))
                .addAll(additional.stream().map(pair -> Pair.of(pair.getLeft().toArray(ItemListing[]::new), pair.getRight())).toList())
                .build();
    }

    /**
     * Posts a VillagerTradesEvent for each registered profession.
     */
    private static void postVillagerEvents(HolderLookup.Provider registries) {
        for (var entry : BuiltInRegistries.VILLAGER_PROFESSION.entrySet()) {
            var prof = entry.getKey();
            Int2ObjectMap<ItemListing[]> trades = VANILLA_TRADES.getOrDefault(prof, new Int2ObjectOpenHashMap<>());
            Int2ObjectMap<List<ItemListing>> mutableTrades = new Int2ObjectOpenHashMap<>();
            for (int i = 1; i < 6; i++) {
                mutableTrades.put(i, NonNullList.create());
            }
            trades.int2ObjectEntrySet().forEach(e -> {
                Arrays.stream(e.getValue()).forEach(mutableTrades.get(e.getIntKey())::add);
            });
            NeoForge.EVENT_BUS.post(new VillagerTradesEvent(mutableTrades, prof, registries));
            Int2ObjectMap<ItemListing[]> newTrades = new Int2ObjectOpenHashMap<>();
            mutableTrades.int2ObjectEntrySet().forEach(e -> newTrades.put(e.getIntKey(), e.getValue().toArray(new ItemListing[0])));
            VillagerTrades.TRADES.put(prof, newTrades);
        }
    }
}
