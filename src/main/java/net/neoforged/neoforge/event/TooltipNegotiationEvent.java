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
import net.neoforged.neoforge.common.tooltip.TooltipDocument;
import net.neoforged.neoforge.common.tooltip.TooltipIntent;
import net.neoforged.neoforge.common.tooltip.TooltipNegotiation;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fired on {@code NeoForge.EVENT_BUS} after the structured tooltip document is built and frozen, before it is
/// flattened for the legacy {@link net.neoforged.neoforge.event.entity.player.ItemTooltipEvent} (item tooltips)
/// or {@link net.neoforged.neoforge.event.entity.player.FluidTooltipEvent} (fluid tooltips). Every listener
/// reads the <em>same</em> immutable {@link TooltipDocument.Snapshot} and submits declarative intents through a
/// {@link TooltipNegotiation} handle obtained from {@link #tooltip(String)}; intents are collected and resolved
/// only after all listeners return, so no listener can change what another sees, and the result never depends on
/// listener registration order.
///
/// Register with a plain {@code NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, ...)} and pass the
/// provider mod id when grabbing the handle &mdash; it is stamped on every intent and used for deterministic
/// tie-breaks and conflict diagnostics:
/// ```java
/// NeoForge.EVENT_BUS.addListener(TooltipNegotiationEvent.class, event -> {
///     var tooltip = event.tooltip("mymod");
///     if (event.getItemStack().is(Items.STICK)) {
///         tooltip.addAfter(TooltipTags.itemName(), Component.literal("Energy: 100"));
///     }
/// });
/// ```
/// Cancellation is disallowed.
public class TooltipNegotiationEvent extends Event {
    private final TooltipDocument.Snapshot snapshot;
    private final ItemStack stack;
    @Nullable
    private final FluidStack fluidStack;
    private final Item.TooltipContext context;
    private final TooltipDisplay display;
    @Nullable
    private final Player player;
    private final TooltipFlag flag;

    private final List<TooltipIntent> intents = new ArrayList<>();
    private final List<TooltipNegotiation> negotiations = new ArrayList<>();
    private long ordinalCounter;

    @ApiStatus.Internal
    public TooltipNegotiationEvent(TooltipDocument.Snapshot snapshot, ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag) {
        this.snapshot = snapshot;
        this.stack = stack;
        this.fluidStack = null;
        this.context = context;
        this.display = display;
        this.player = player;
        this.flag = flag;
    }

    @ApiStatus.Internal
    public TooltipNegotiationEvent(TooltipDocument.Snapshot snapshot, FluidStack fluidStack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag) {
        this.snapshot = snapshot;
        this.stack = ItemStack.EMPTY;
        this.fluidStack = fluidStack;
        this.context = context;
        this.display = display;
        this.player = player;
        this.flag = flag;
    }

    /// The immutable document every listener reads.
    public TooltipDocument.Snapshot snapshot() {
        return snapshot;
    }

    /// {@return true when the negotiated tooltip belongs to a fluid stack rather than an item stack}
    public boolean isFluid() {
        return fluidStack != null;
    }

    /// The item stack whose tooltip is negotiated, or {@link ItemStack#EMPTY} for fluid tooltips.
    public ItemStack getItemStack() {
        return stack;
    }

    /// The fluid stack whose tooltip is negotiated, or {@code null} for item tooltips.
    @Nullable
    public FluidStack getFluidStack() {
        return fluidStack;
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

    /// A fresh negotiation handle for the calling listener, stamped with its provider mod id. The handle shares
    /// the event-wide ordinal source so intents from multiple listeners of one provider never collide; pending
    /// fluent edits are flushed when the dispatch ends, so a listener may configure them until it returns.
    public TooltipNegotiation tooltip(String providerModId) {
        var negotiation = new TooltipNegotiation(snapshot, providerModId, this::nextOrdinal);
        negotiations.add(negotiation);
        return negotiation;
    }

    private long nextOrdinal() {
        return ordinalCounter++;
    }

    /// Flush every handed-out negotiation and return the intents collected from all listeners, in submission order.
    @ApiStatus.Internal
    public List<TooltipIntent> collectedIntents() {
        for (TooltipNegotiation negotiation : negotiations) {
            intents.addAll(negotiation.collectIntents());
        }
        negotiations.clear();
        return List.copyOf(intents);
    }
}
