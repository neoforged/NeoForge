/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.tooltip.TooltipIntent;
import net.neoforged.neoforge.common.tooltip.TooltipNegotiation;
import net.neoforged.neoforge.common.tooltip.TooltipSnapshot;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fired on {@code NeoForge.EVENT_BUS} after the structured tooltip document is built and frozen, before it is
/// flattened for the legacy {@link net.neoforged.neoforge.event.entity.player.ItemTooltipEvent}. Every listener
/// reads the <em>same</em> immutable {@link TooltipSnapshot} and submits intents via {@link #tooltip()}; intents
/// are collected and resolved only after all listeners return, so no listener can change what another sees, and
/// the result never depends on listener registration order.
///
/// Listeners register with a plain {@code NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, ...)}.
/// The provider id stamped on every intent (used for deterministic tie-breaks and conflict diagnostics) is
/// {@code "unknown"} unless set via {@link #bindProvider} &mdash; the tooltip pipeline is expected to bind the
/// firing listener's mod before invoking it. Cancellation is disallowed.
public class TooltipNegotiationEvent extends Event {
    private final TooltipSnapshot snapshot;
    private final ItemStack stack;
    private final Item.TooltipContext context;
    private final TooltipDisplay display;
    @Nullable
    private final Player player;
    private final TooltipFlag flag;

    private final List<TooltipIntent> intents = new ArrayList<>();
    @Nullable
    private TooltipNegotiation current;

    @ApiStatus.Internal
    public TooltipNegotiationEvent(TooltipSnapshot snapshot, ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag) {
        this.snapshot = snapshot;
        this.stack = stack;
        this.context = context;
        this.display = display;
        this.player = player;
        this.flag = flag;
    }

    /// The immutable document every listener reads.
    public TooltipSnapshot snapshot() {
        return snapshot;
    }

    public ItemStack getItemStack() {
        return stack;
    }

    public Item.TooltipContext getContext() {
        return context;
    }

    public TooltipDisplay getDisplay() {
        return display;
    }

    @Nullable
    public Player getEntity() {
        return player;
    }

    public TooltipFlag getFlags() {
        return flag;
    }

    /// The per-listener negotiation handle. Bind/commit are driven by the registration helper.
    public TooltipNegotiation tooltip() {
        if (current == null) {
            // A bare listener (registered without the mod-stamping helper): degrade gracefully.
            current = new TooltipNegotiation(snapshot, "unknown");
        }
        return current;
    }

    @ApiStatus.Internal
    public void bindProvider(String providerModId) {
        this.current = new TooltipNegotiation(snapshot, providerModId);
    }

    @ApiStatus.Internal
    public void commitCurrent() {
        if (current != null) {
            intents.addAll(current.collectIntents());
            current = null;
        }
    }

    /// The intents collected from every listener so far, in submission order.
    @ApiStatus.Internal
    public List<TooltipIntent> collectedIntents() {
        return List.copyOf(intents);
    }
}
