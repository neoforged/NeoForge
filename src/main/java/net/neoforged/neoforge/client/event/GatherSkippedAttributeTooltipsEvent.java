/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is used to collect UUIDs of attribute modifiers that will not be displayed in item tooltips.
 * <p>
 * This allows hiding specific modifiers for whatever reason. They will still be shown in the attributes GUI.
 * <p>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class GatherSkippedAttributeTooltipsEvent extends PlayerEvent {
    protected final ItemStack stack;
    protected final Set<ResourceLocation> skips;
    protected final TooltipFlag flag;

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack, @Nullable Player player, Set<ResourceLocation> skips, TooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.skips = skips;
        this.flag = flag;
    }

    /**
     * Use to determine if the advanced information on item tooltips is being shown, toggled by F3+H.
     */
    public TooltipFlag getFlags() {
        return this.flag;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Mark the ResourceLocation of a specific attribute modifier as skipped, causing it to not be displayed in the tooltip.
     */
    public void skipID(ResourceLocation id) {
        this.skips.add(id);
    }

    /**
     * This event is fired with a null player during startup when populating search trees for tooltips.
     */
    @Override
    @Nullable
    public Player getEntity() {
        return super.getEntity();
    }
}
