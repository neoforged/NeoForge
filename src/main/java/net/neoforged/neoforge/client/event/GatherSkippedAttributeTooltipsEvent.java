/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * This event is used to collect the IDs of attribute modifiers that will not be displayed in item tooltips.
 * <p>
 * It allows hiding some (or all) of the modifiers, potentially for displaying them in an alternative way (or for hiding information from the player).
 * <p>
 * This event may be fired on both the logical client and logical server.
 */
public class GatherSkippedAttributeTooltipsEvent extends Event {
    protected final ItemStack stack;
    protected final Set<ResourceLocation> skippedIds;
    protected final Set<EquipmentSlotGroup> skippedGroups;
    protected final AttributeTooltipContext ctx;
    protected boolean skipAll = false;

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack, AttributeTooltipContext ctx) {
        this.stack = stack;
        this.skippedIds = new HashSet<>();
        this.skippedGroups = EnumSet.noneOf(EquipmentSlotGroup.class);
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
        this.skippedIds.add(id);
    }

    /**
     * Marks an entire {@link EquipmentSlotGroup} as skipped, preventing all modifiers for that group from showing.
     */
    public void skipGroup(EquipmentSlotGroup group) {
        this.skippedGroups.add(group);
    }

    /**
     * Checks if a given id is skipped or not. If all modifiers are skipped, this method always returns true.
     */
    public boolean isSkipped(ResourceLocation id) {
        return this.skipAll || this.skippedIds.contains(id);
    }

    /**
     * Checks if a given group is skipped or not. If all modifiers are skipped, this method always returns true.
     */
    public boolean isSkipped(EquipmentSlotGroup group) {
        return this.skipAll || this.skippedGroups.contains(group);
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
