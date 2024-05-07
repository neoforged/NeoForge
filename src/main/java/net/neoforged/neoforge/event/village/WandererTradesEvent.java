/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.village;

import java.util.List;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

/**
 * WandererTradesEvent is fired during reload by {@link TagsUpdatedEvent}. It is used to gather the trade lists for the wandering merchant.
 * It is fired on the {@link NeoForge#EVENT_BUS}.
 * The wandering merchant picks a few trades from {@code generic} and a single trade from {@code rare}.
 * To add trades to the merchant, simply add new trades to the list. {@link BasicItemListing} provides a default implementation.
 */
public class WandererTradesEvent extends Event {
    protected List<ItemListing> generic;
    protected List<ItemListing> rare;

    public WandererTradesEvent(List<ItemListing> generic, List<ItemListing> rare) {
        this.generic = generic;
        this.rare = rare;
    }

    public List<ItemListing> getGenericTrades() {
        return generic;
    }

    public List<ItemListing> getRareTrades() {
        return rare;
    }
}
