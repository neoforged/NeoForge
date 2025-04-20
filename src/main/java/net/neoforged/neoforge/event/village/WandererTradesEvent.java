/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.village;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

/**
 * WandererTradesEvent is fired during reload by {@link TagsUpdatedEvent}. It is used to gather the trade lists for the wandering merchant.
 * It is fired on the {@link NeoForge#EVENT_BUS}.
 * For each set of trades the wandering merchant picks the specified amount of trades from the corresponding list.
 * To add trades to the merchant, simply add new trades to the list. {@link BasicItemListing} provides a default implementation.
 */
public class WandererTradesEvent extends Event {
    protected List<ItemListing> buying;
    protected int buyingAmount;
    protected List<ItemListing> rare;
    protected int rareAmount;
    protected List<ItemListing> generic;
    protected int genericAmount;
    protected List<Pair<List<ItemListing>, Integer>> additionalTrades;
    private final HolderLookup.Provider registries;

    @ApiStatus.Internal
    public WandererTradesEvent(List<ItemListing> buying, int buyingAmount, List<ItemListing> rare, int rareAmount, List<ItemListing> generic, int genericAmount, List<Pair<List<ItemListing>, Integer>> additionalTrades, Provider registries) {
        this.buying = buying;
        this.buyingAmount = buyingAmount;
        this.rare = rare;
        this.rareAmount = rareAmount;
        this.generic = generic;
        this.genericAmount = genericAmount;
        this.additionalTrades = additionalTrades;
        this.registries = registries;
    }

    public List<ItemListing> getBuyingTrades() {
        return buying;
    }

    public int getBuyingAmount() {
        return buyingAmount;
    }

    public void setBuyingAmount(int amount) {
        buyingAmount = amount;
    }

    public List<ItemListing> getRareTrades() {
        return rare;
    }

    public int getRareAmount() {
        return rareAmount;
    }

    public void setRareAmount(int amount) {
        rareAmount = amount;
    }

    public List<ItemListing> getGenericTrades() {
        return generic;
    }

    public int getGenericAmount() {
        return genericAmount;
    }

    public void setGenericAmount(int amount) {
        genericAmount = amount;
    }

    public void addTrades(List<ItemListing> trades, int amount) {
        additionalTrades.add(Pair.of(trades, amount));
    }

    public HolderLookup.Provider getRegistries() {
        return registries;
    }
}
