/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

public class FluidTooltipEvent extends PlayerEvent {
    private final TooltipFlag flags;
    private final FluidStack fluidStack;
    private final List<Component> toolTip;
    private final TooltipContext context;

    /**
     * This event is fired in {@link FluidStack#getTooltipLines(TooltipContext, Player, TooltipFlag)}, which in turn is called from its respective modded GUI.
     * Tooltips may also be gathered with a null player during startup by other mods.
     */
    public FluidTooltipEvent(FluidStack fluidStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
        super(player);
        this.fluidStack = fluidStack;
        this.toolTip = list;
        this.flags = flags;
        this.context = context;
    }

    /**
     * Use to determine if the advanced information on fluid tooltips is being shown, toggled by F3+H.
     */
    public TooltipFlag getFlags() {
        return flags;
    }

    /**
     * The {@link FluidStack} with the tooltip.
     */
    public FluidStack getFluidStack() {
        return fluidStack;
    }

    /**
     * The {@link FluidStack} tooltip.
     */
    public List<Component> getToolTip() {
        return toolTip;
    }

    /**
     * This event is fired with a null player during startup when populating search trees for tooltips.
     */
    @Override
    @Nullable
    public Player getEntity() {
        return super.getEntity();
    }

    /**
     * The {@link TooltipContext tooltip context}.
     */
    public TooltipContext getContext() {
        return context;
    }
}
