/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public class DataComponentTooltipEvent<T extends TooltipProvider> extends PlayerEvent {
    private final TooltipFlag flags;
    private final ItemStack stack;
    private final TooltipDisplay display;
    private final TooltipContext context;
    private final Consumer<DataComponentType<T>> tooltipSetter;

    /// This event is fired in {@link ItemStack#addDetailsToTooltip(TooltipContext, TooltipDisplay, Player, TooltipFlag, Consumer)} when hovering over an item on the currently open Screen.
    /// Tooltips are also gathered with a null player during startup by {@link SessionSearchTrees#getTooltipLines(java.util.stream.Stream, TooltipContext, TooltipFlag)}.
    ///
    /// For a more generic version of this event that allows arbitrary data to be added to tooltips, see {@link ItemTooltipEvent}.
    public DataComponentTooltipEvent(ItemStack stack, @Nullable Player player, TooltipDisplay display, TooltipFlag flags, TooltipContext context, Consumer<DataComponentType<T>> tooltipSetter) {
        super(player);
        this.stack = stack;
        this.flags = flags;
        this.display = display;
        this.context = context;
        this.tooltipSetter = tooltipSetter;
    }

    /// The {@link TooltipFlag tooltip flag}. Can be used to check if different keys are held down or if advanced tooltips are shown.
    public TooltipFlag getFlags() {
        return this.flags;
    }

    /// The {@link ItemStack} with the tooltip.
    public ItemStack getItemStack() {
        return this.stack;
    }

    /// This event is fired with a null player during startup when populating search trees for tooltips.
    @Override
    @Nullable
    public Player getEntity() {
        return super.getEntity();
    }

    /// The {@link TooltipContext tooltip context}.
    public TooltipContext getContext() {
        return this.context;
    }

    /// The {@link TooltipDisplay tooltip display} for this item. Determines whether components are hidden or not.
    ///
    /// {@link ItemStack#addToTooltip} does already check that the given component is not hidden. This is here in case other conditions need to be checked.
    public TooltipDisplay getDisplay() {
        return this.display;
    }

    /// Adds a tooltip to an item via {@link ItemStack#addToTooltip}.
    ///
    /// Keep in mind there is no special ordering done between mods, but components added here will always render above vanilla ones.
    public void addComponentToTooltip(DataComponentType<T> component) {
        this.tooltipSetter.accept(component);
    }
}
