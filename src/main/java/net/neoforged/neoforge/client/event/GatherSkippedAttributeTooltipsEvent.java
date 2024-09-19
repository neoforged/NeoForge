/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.util.TooltipUtil.AttributeTooltipContext;

/**
 * This event is used to collect the IDs of attribute modifiers that will not be displayed in item tooltips.
 * <p>
 * It allows hiding some (or all) of the modifiers, potentially for displaying them in an alternative way (or for hiding information from the player).
 * <p>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class GatherSkippedAttributeTooltipsEvent extends Event {
    protected final ItemStack stack;
    protected final Set<ResourceLocation> skips;
    protected final AttributeTooltipContext ctx;
    protected boolean skipAll = false;

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack, Set<ResourceLocation> skips, AttributeTooltipContext ctx) {
        this.stack = stack;
        this.skips = skips;
        this.ctx = ctx;
    }

    /**
     * The current tooltip context.
     */
    public AttributeTooltipContext getContext() {
        return this.ctx;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Marks the id of a specific attribute modifier as skipped, causing it to not be displayed in the tooltip.
     */
    public void skipId(ResourceLocation id) {
        this.skips.add(id);
    }

    /**
     * Checks if a given id is skipped or not. If all modifiers are skipped, this method always returns true.
     */
    public boolean isSkipped(ResourceLocation id) {
        return this.skipAll || this.skips.contains(id);
    }

    /**
     * Sets if the event should skip displaying all attribute modifiers.
     */
    public void setSkipAll(boolean skip) {
        this.skipAll = skip;
    }

    /**
     * Checks if the event will cause all attribute modifiers to be skipped.
     */
    public boolean isSkippingAll() {
        return this.skipAll;
    }
}
