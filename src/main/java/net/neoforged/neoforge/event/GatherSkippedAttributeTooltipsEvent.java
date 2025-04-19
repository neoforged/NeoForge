/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import org.jetbrains.annotations.Nullable;

/**
 * This event is used to collect the IDs of attribute modifiers that will not be displayed in item tooltips.
 * <p>
 * It allows hiding some (or all) of the modifiers, potentially for displaying them in an alternative way (or for hiding information from the player).
 * <p>
 * This event may be fired on both the logical client and logical server.
 */
public class GatherSkippedAttributeTooltipsEvent extends Event {
    protected final ItemStack stack;
    protected final AttributeTooltipContext ctx;

    @Nullable
    private Set<ResourceLocation> skippedIds = null;

    @Nullable
    private Set<EquipmentSlotGroup> skippedGroups = null;

    private boolean skipAll = false;

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack, AttributeTooltipContext ctx) {
        this.stack = stack;
        this.ctx = ctx;
        // Skip sets are lazily initialized by the getter functions to avoid memory churn
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
        this.getSkippedIds().add(id);
    }

    /**
     * Marks an entire {@link EquipmentSlotGroup} as skipped, preventing all modifiers for that group from showing.
     */
    public void skipGroup(EquipmentSlotGroup group) {
        this.getSkippedGroups().add(group);
    }

    /**
     * Checks if a given id is skipped or not. If all modifiers are skipped, this method always returns true.
     */
    public boolean isSkipped(ResourceLocation id) {
        return this.skipAll || (this.skippedIds != null && this.skippedIds.contains(id));
    }

    /**
     * Checks if a given group is skipped or not. If all modifiers are skipped, this method always returns true.
     */
    public boolean isSkipped(EquipmentSlotGroup group) {
        return this.skipAll || (this.skippedGroups != null && this.skippedGroups.contains(group));
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

    /**
     * Initializes {@link #skippedIds} if necessary, and returns it.
     */
    protected Set<ResourceLocation> getSkippedIds() {
        if (this.skippedIds == null) {
            this.skippedIds = new HashSet<>();
        }
        return this.skippedIds;
    }

    /**
     * Initializes {@link #skippedGroups} if necessary, and returns it.
     */
    protected Set<EquipmentSlotGroup> getSkippedGroups() {
        if (this.skippedGroups == null) {
            this.skippedGroups = EnumSet.noneOf(EquipmentSlotGroup.class);
        }
        return this.skippedGroups;
    }
}
